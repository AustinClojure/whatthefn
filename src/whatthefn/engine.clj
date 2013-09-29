(ns whatthefn.engine
  (:use [whatthefn.functions :as fxns]
        [whatthefn.messages :as msgs]
        [whatthefn.events :as evs]
        [whatthefn.submit :as subm])
  (:require [clojure.core.async :refer :all]))

;;initialize state

(defn new-room [name]
  {:name name :current-func nil :seen-functions '() :players #{} :state :waiting-for-players :winners #{} :channel nil :round-id 1})

(defn get-initial-state [name]
  {:rooms {name (new-room name)}})

;;outgoing messages

(defn send-player-in-room [room-id player-name status]
  (msgs/new-message! room-id {:type :player-in-room :room room-id :player player-name :in-room? status}))

(defn send-fn-resolve-result [room-id player input output]
  (msgs/new-message! room-id {:type :resolve-input :input input :output output :player player}))

(defn send-fn-answer-result [room-id player-name result points]
  (msgs/new-message! room-id {:type :answer-solution :player player-name :room room-id :result result :points-awarded points}))

(defn send-round-ends [room-id]
  (msgs/new-message! room-id {:type :round-ends :room room-id}))

(defn send-round-begins [room-id round-data]
  (msgs/new-message! room-id {:type :round-begins :room room-id :round-data round-data}))

(defn send-player-scored [room-id player-name num-points]
  (msgs/new-message! room-id {:type :player-scored :room room-id :player player-name :points num-points}))

;;self messages

(defn send-message-self [channel message]
  (prn "trying to send self:" message)
  (go (>!! channel message)))

;;state util

(defn get-game-state [state room-id]
  (get-in state [:rooms room-id :state]))

(defn get-current-function [state room-id]
  (let [rooms (:rooms state)
        room (rooms room-id)]
    (:current-func room)))

(defn build-room-data [state room-id]
  (let [f (get-current-function state room-id)]
    {:name (:name f) :type (:type f) :description (:description f)}))

(defn num-players [state room-id]
  "return the number of players in the room"
  (let [rooms (:rooms state)
        room (rooms room-id)]
    (count (:players room))))

(defn num-winners [state room-id]
  "return the number of players that have won the round"
  (let [rooms (:rooms state)
        room (rooms room-id)]
    (count (:winners room))))

(defn player-in-room? [state room-id player]
  "returns true if a player is in the given room"
  (let [rooms (:rooms state)
        room (rooms room-id)]
    (contains? (:players room) player)))

(defn room-full? [state room-id]
  "return true if the room is full"
  (= 4 (num-players state room-id)))

(defn player-winner? [state room-id player-id]
  "is this player alread a winner for this round?"
  (let [rooms (:rooms state)
        room (rooms room-id)
        winners (:winners room)]
    (contains? winners player-id)))

(defn get-score-value [state room-id player result]
  "returns how many points it is worth to score now"
  (if (and result (not (player-winner? state room-id player)))
    (+ 2 (- (num-players state room-id) (num-winners state room-id)))
    0))

(defn everyone-won? [state room-id]
  (let [winners (get-in state [:rooms room-id :winners])
        players (get-in state [:rooms room-id :players])]
    (empty? (clojure.set/difference players winners))))

(defn get-channel [state room-id]
  (get-in state [:rooms room-id :channel]))

(defn get-room-state [state room-id]
  (get-in state [:rooms room-id :state]))

;;state updates(engine logic)

(defn clear-winners [state room-id]
  (assoc-in state [:rooms room-id :winners] #{}))

(defn refresh-function [state room-id]
  (update-in state [:rooms room-id :current-func] fxns/get-next-function))

(defn reset-round [state room-id]
  (let [winners-cleared (clear-winners state room-id)
        function-added (refresh-function winners-cleared room-id)
        round-id-inced (update-in function-added [:rooms room-id :round-id] inc)
        state-reset (assoc-in round-id-inced [:rooms room-id :state] :waiting-for-players)]
    state-reset))

(defn game-starts [state room-id]
  (prn "game starts!")
  (.start (Thread.
           (Thread/sleep 10000)
           (send-message-self (get-channel state room-id) {:type :force-end :room room-id :round-id (get-in state [:rooms room-id :channel])})))
  (send-round-begins room-id (build-room-data state room-id))
  (assoc-in state [:rooms room-id :state] :round-playing))

(defn check-game-starts [state room-id]
  (let [game-state (get-room-state state room-id)]
    (cond
     (and (= game-state :waiting-for-players) (> (num-players state room-id) 0)) (game-starts state room-id)
     :else state)))

(defn game-ends [state room-id]
  (prn "game ends!")
  (send-round-ends room-id)
  (let [reset-state (reset-round state room-id)]
    (check-game-starts reset-state room-id)))

(defn check-game-ends [state room-id]
  (let [game-state (get-room-state state room-id)]
    (cond
     (and (= game-state :round-playing) (= (num-players state room-id) 0)) (game-ends state room-id)
     (and (= game-state :round-playing) ( everyone-won? state room-id)) (game-ends state room-id)
     :else state)))

(defn remove-player-room [state room-id player-name]
  (let [rooms (:rooms state)
        room (rooms room-id)
        players (:players room)
        removed-state (assoc-in state [:rooms room-id :players] (disj players player-name))]
    (check-game-ends removed-state room-id)))

(defn add-player-room [state room-id player-name]
  (let [rooms (:rooms state)
        room (rooms room-id)
        players (:players room)]
    (if (and (not (player-in-room? state room-id player-name)) (< (count players) 4))
      (let [player-added-state (update-in state [:rooms room-id :players] #(conj % player-name))]
        (check-game-starts player-added-state room-id))
      state)))

(defn player-won [state room-id player]
  (prn "player won!!")
  (prn room-id)
  (prn player)
  (let [new-room (update-in state [:rooms room-id :winners] conj player)]
    (game-ends new-room room-id)))

;;message processing

(defmulti proc-message #(:type %2))

(defmethod proc-message :resolve-input [state msg]
  "we got an input to test"
  (let [room-id (:room msg)
        player (:player msg)
        room (get-in state [:rooms room-id])
        arg (:arg msg)
        func (:current-func room)]
                                        ;(subm/submit-value-engine arg (:body (get-current-function state room)) (partial send-fn-resolve-result room arg))
    (fxns/test-fun (:id func) arg (partial send-fn-resolve-result room-id player arg))
    state))

(defmethod proc-message :test-solution [state msg]
  "we got a function to grade"
  (let [f (:function msg)
        room (:room msg)]
    (subm/submit-fn-engine f (get-current-function state room) (partial send-message-self (get-channel state room)) msg)
    state))

(defmethod proc-message :function-eval-result [state cb]
  "we got our own message about a function grade back"
  (let [msg (:orig cb)
        f (:function msg)
        room (:room msg)
        player (:player msg)
        id (:func-id msg)
        res (:result cb)
        points-scored (get-score-value state room player res)]
    (send-fn-answer-result room player res points-scored)
    (if (> points-scored 0)
      (player-won state room player)
      state)))

(defmethod proc-message :player-join-attempt [state msg]
  (let [room-id (:room msg)
        player-name (:player msg)
        new-room (add-player-room state room-id player-name)]
    (send-player-in-room room-id player-name (player-in-room? new-room room-id player-name))
    new-room))

(defmethod proc-message :player-left [state msg]
  (let [room-id (:room msg)
        player-name (:player msg)
        new-room (remove-player-room state room-id player-name)]
    new-room))

(defmethod proc-message :force-end [state msg]
  (let [room-id (:room msg)
        round-id (:round-id msg)]
    (if (= round-id (get-in state [:rooms room-id :round-id]))
      (game-ends state room-id)
      state)))

(defmethod proc-message :tick [state msg]
  state)

(defn start-engine [room-id]
  (let [game-channel (evs/game-channel room-id)
        state-transition-function proc-message
        initial-state (get-initial-state room-id)
        state-with-function (refresh-function initial-state room-id)
        state-with-channel (assoc-in state-with-function [:rooms room-id :channel] game-channel)]
    (go
      (loop [game-state state-with-channel]
        (let [next-event (<!! game-channel)]
          (prn "got event:" next-event)
          (prn "current state:" game-state)
          ;write broadcast message here
          (recur (state-transition-function game-state next-event)))))))
