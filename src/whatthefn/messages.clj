(ns whatthefn.messages)

(def room-messages
  (ref {}))

(defn new-room!
  [room-id]
  (alter room-messages assoc room-id (ref [])))

(defn annotate-message-with-id
  [message]
  (assoc message :id (str (gensym ""))))

(defn new-message!
  "Creates a room with the given id if it doesn't exist. Then associates an :id
   with the message and appends it to the list of messages for that room"
  [room-id message]
  (dosync
    (when-not (@room-messages room-id)
      (new-room! room-id)))
  (dosync
    (let [message-with-id (annotate-message-with-id message)]
      (alter (@room-messages room-id) conj message-with-id)
      {:id (:id message-with-id)})))

(defn messages-since
  "Returns all messages in the provided room-id that have happened since the
   message with the given id. If message-id is nil, returns all messags for
   that room."
  [room-id message-id]
  (if-let [messages (@room-messages room-id)]
    (if message-id
      (rest (drop-while #(not= message-id (:id %)) @messages))
      @messages)))