(ns whatthefn.engine
  :use [whatthefn.functions :as fxns
        whatthefn.messages :as msgs])

;;initialize state

(defn new-room [name]
  {:name name :current-func nil :seen-functions '() :player-names #{} :state :waiting-for-players :winners #{}})

(defn new-player [name]
  {:name name :score 0})

(defn get-initial-state []
  {:rooms {:the-room (new-room :the-room)
           :players #{}}})

;;outgoing messages

(defn send-join-confirm [room-id player-name]
  (msgs/new-message! room-id {:type :join-confirm :room room-id :player player-name}))

(defn send-join-reject [room-id player-name]
  (msgs/new-message! room-id {:type :join-reject :room room-id :player player-name}))

(defn send-fn-resolve-result [input output room-id]
  (msgs/new-message! room-id {:type :resolve-input :input input :output output}))

(defn send-fn-answer-result [room-id player-name result]
  (msgs/new-message! room-id {:type :grade-answer :player player-name :room room-id :result result}))

(defn send-round-ends [room-id]
  (msgs/new-message! room-id {:type :round-ends :room room-id}))

(defn send-round-begins [room-id round-data]
  (msgs/new-message! room-id {:type :round-begins :room room-id :round-data round-data}))

(defn send-player-scored [room-id player-name num-points]
  (msgs/new-message! room-id {:type :player-scored :room room-id :player player-name :points num-points}))

;;state util

(defn num-players [state room-id]
  (let [rooms (:rooms state)
        room (rooms room-id)]
    (count (:players room))))

(defn num-winners [state room-id]
  (let [rooms (:rooms state)
        room (rooms room-id)]
    (count (:winners room))))

(defn get-score-value [state room-id]
  (+ 2 (- (num-players) (num-winners))))

(defn player-in-room? [state room-id player]
  (let [rooms (:rooms state)
        room (rooms room-id)]
    (contains? (:players room) player)))

(defn check-room-full [state room-id]
  (= 4 (num-players state room-id)))

(defn player-winner? [state room-id player-id]
  "is this player alread a winner for this round?"
  (let [rooms (:rooms state)
        room (rooms room-id)
        winners (:winners room)]
    (contains? winners player-id)))

;;state updates(engine logic)

(defn add-player-room [state room-id player-name]
  (let [rooms (:rooms state)
        room (rooms room-id)
        players (:players room)]
    (if (and (not (player-in-room? state room-id player-name)) (< (count players) 4))
      (update-in state [:rooms room-id :players] #(conj % player-name))
      state)))

(defn player-join-attempt [room-id player-name state]
  (let [rooms (:rooms state)
        room (rooms room-id)]))

(defn player-won [state player-id]
  "the player correctly answered...if all players have answered, end round"
  ())

(defn refresh-function [state room-id]
  (update-in state [:rooms room-id :current-func] fxns/get-next-function))

(defn clear-winners [state room-id]
  (update-in state [:rooms room-id :winners] #{}))

(defn reset-round [state room-id]
  (let [winners-cleared (clear-winners state room-id)
        function-added ()]))

;;message processing

(defmulti proc-message #(:type %2))

(defmethod proc-message :resolve-input [state msg]
  (let [room (:room msg)
        player (:player msg)
        arg (:arg msg)
        id (:func-id msg)]
    (send-fn-resolve-result arg (fxns/eval-function id arg) room)
    state))

(defmethod proc-message :test-answer [state msg]
  (let [f (:function msg)
        room (:room msg)
        player (:player msg)
        id (:func-id msg)
        res (fxns/test-function id f)]
    (send-fn-answer-result room player res)
    (if res
      (player-won state player)
      state)))

(defn get-lazy-seq-http [])

(defn start-engine []
  (reduce proc-message state (get-lazy-seq-http)))
