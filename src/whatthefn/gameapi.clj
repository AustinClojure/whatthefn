(ns whatthefn.gameapi
  (:use [whatthefn.messages :only [new-message!]]
        [whatthefn.events :only [write-to-game-channel!]]))

(defn chat
  [{:keys [room-id player string]}]
  (new-message! room-id
                {:type :chat
                 :player player
                 :string string}))

(defn resolve-input
  [{:keys [room-id player arg]}]
  (write-to-game-channel! room-id
                          {:type :resolve-input
                           :player player
                           :arg arg}))

(defn test-solution
  [{:keys [room-id player function]}]
  (write-to-game-channel! room-id
                          {:type :test-solution
                           :player player
                           :function function}))

(defn handler
  [req]
  (case (:type (:params req))
    "chat" (chat (:params req))
    "resolve-input" (resolve-input (:params req))
    "test-solution" (test-solution (:params req))))