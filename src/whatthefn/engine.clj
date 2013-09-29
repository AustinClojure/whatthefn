(ns whatthefn.engine
  (:use [whatthefn.functions :as fxns]
        [whatthefn.messages :as msgs]
        [whatthefn.events :as evs]
        [whatthefn.submit :as subm])
  (:require [clojure.core.async :refer :all]))

;;initialize state

(defn new-room [name]
  {:name name :current-func nil :seen-functions '() :players #{} :state :waiting-for-players :winners #{} :channel nil})

(defn get-initial-state []
  {:rooms {"the-room" (new-room "the-room")}})

;;outgoing messages

(defn send-player-in-room [room-id player-name status]
  (msgs/new-message! room-id {:type :player-in-room :room room-id :player player-name :in-room? status}))

(defn send-fn-resolve-result [room-id input output]
  (msgs/new-message! room-id {:type :resolve-input :input input :output output}))

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
  (go #(>!! channel message)))

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
        state-reset (assoc-in function-added [:rooms room-id :state] :waiting-for-players)]
    function-added))

(defn game-starts [state room-id]
  (send-round-begins room-id (build-room-data state room-id))
  (assoc-in state [:rooms room-id :state] :round-playing))

(defn game-ends [state room-id]
  (send-round-ends room-id)
  (reset-round state room-id))

(defn check-game-starts [state room-id]
  (let [game-state (get-room-state state room-id)]
    (cond
     (= game-state :waiting-for-players) (> (num-players state room-id) 0)
     :else false)))

(defn check-game-ends [state room-id]
  (let [game-state (get-room-state state room-id)]
    (cond
     (and (= game-state :round-playing) (= (num-players state room-id) 0)) true
     (everyone-won? state room-id) true
     :else false)))

(defn remove-player-room [state room-id player-name]
  (let [rooms (:rooms state)
        room (rooms room-id)
        players (:players room)
        removed-state (assoc-in state [:rooms room-id :players] (disj players player-name))]
    (if (check-game-ends removed-state room-id)
      (game-ends removed-state room-id)
      removed-state)))

(defn add-player-room [state room-id player-name]
  (let [rooms (:rooms state)
        room (rooms room-id)
        players (:players room)]
    (if (and (not (player-in-room? state room-id player-name)) (< (count players) 4))
      (let [player-added-state (update-in state [:rooms room-id :players] #(conj % player-name))]
        (if (check-game-starts player-added-state room-id)
          (game-starts player-added-state room-id)
          player-added-state))
      state)))

(defn player-won [state room-id player]
  (let [new-room (update-in state [:rooms room-id :winners] conj player)]
    (if (everyone-won? new-room room-id)
      (send-round-ends room-id)
      new-room)))

;;message processing

(defmulti proc-message #(:type %2))

(defmethod proc-message :resolve-input [state msg]
  "we got an input to test"
  (let [room-id (:room msg)
        room (get-in state [:rooms room-id])
        arg (:arg msg)
        func (:current-func room)]
                                        ;(subm/submit-value-engine arg (:body (get-current-function state room)) (partial send-fn-resolve-result room arg))
    (fxns/test-fun (:id func) arg (partial send-fn-resolve-result room-id arg))
    state))

(defmethod proc-message :test-solution [state msg]
  "we got a function to grade"
  (let [f (:function msg)
        room (:room msg)]
    (subm/submit-fn-engine f (:body (get-current-function state room)) (partial send-message-self (get-channel state room)) msg)))

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
    (player-won state room player)))

(defmethod proc-message :player-join-attempt [state msg]
  (let [room-id (:room msg)
        player-name (:player msg)
        new-room (add-player-room state room-id player-name)]
    (send-player-in-room room-id player-name (player-in-room? state room-id player-name))
    new-room))

(defmethod proc-message :player-left [state msg]
  (let [room-id (:room msg)
        player-name (:player msg)
        new-room (remove-player-room state room-id player-name)]
    new-room))

(defmethod proc-message :tick [state msg]
  state)

(defn start-engine []
  (let [room-id "the-room"
        game-channel (evs/game-channel room-id)
        state-transition-function proc-message
        initial-state (get-initial-state)
        state-with-function (refresh-function initial-state room-id)
        state-with-channel (assoc-in state-with-function [:rooms room-id :channel] game-channel)]
    (go
      (loop [game-state state-with-channel]
        (let [next-event (<!! game-channel)]
          (prn next-event)
          (prn game-state)
          ;write broadcast message here
          (recur (state-transition-function game-state next-event)))))))
