(ns whatthefn.engine)

(defn new-room [name]
  {:the-room {:name name :current-func nil :seen-functions '() :player-names '() :state :waiting-for-players}})

;;incoming messages

(defn player-join-attempt [room-id player-name state]
  (let [room (rooms room-id)]))

(defn add-player-room [room-id player-name state])

;;outgoing messages

(defn send-join-confirm [room-id player-name]
  (whatthefn.messages/new-message! room-id {:type :join-confirm :room room-id :player player-name}))

(defn send-join-reject [room-id player-name]
  (whatthefn.messages/new-message! room-id {:type :join-reject :room room-id :player player-name}))

(defn send-fn-resolve-result [input output room-id]
  (whatthefn.messages/new-message! room-id {:type :resolve-input :input input :output output}))

(defn send-fn-answer-result [room-id player-name result]
  (whatthefn.messages/new-message! room-id {:type :grade-answer :player player-name :room room-id :result result}))

(defmulti proc-message :type)

(defmethod proc-message :resolve-input [msg]
  (let [room (:room msg)
        player (:player msg)
        arg (:arg msg)
        id (:func-id msg)]
    (send-fn-resolve-result arg (whatthefn.functions/eval-function id arg) room)))

(defmethod proc-message :test-answer [msg]
  (let [f (:function msg)
        room (:room msg)
        player (:player msg)
        id (:func-id msg)]
    (send-fn-answer-result room player (whatthefn.functions/test-function id f))))

(defn start-engine [])
