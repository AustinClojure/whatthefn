(ns whatthefn.gameapi
  (:use [whatthefn.messages :only [new-message!]]))

(defn chat
  [{:keys [room-id player string]}]
  (new-message! room-id
                {:type :chat
                 :player player
                 :string string}))

(defn handler
  [req]
  (cond (= "chat" (:type (:params req))) (chat (:params req))))