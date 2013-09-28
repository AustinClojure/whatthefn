(ns whatthefn.engine)

(defn new-room [name]
  {:the-room {:name name :current-func nil :seen-functions '() :player-names '() :state :waiting-for-players}})

;;incoming messages

(defn player-join-attempt [room-id player-name state]
  (let [room (rooms room-id)]))

(defn add-player-room [room-id player-name state])

;;outgoing messages

(defn send-join-confirm [room-id player-name])

(defn send-join-reject [room-id player-name])

(defn send-fn-resolve-result [input output room-id])

(defn send-fn-answer-result [room-id player-name result])

(defmulti proc-message :type)
(defmethod proc-message :resolve-input [msg])
(defmethod proc-message :test-answer [msg]
  (let [f (:function msg)
        room (:room msg)
        player (:player msg)]
    (send-fn-answer-result room player whatthefn.functions/)))
