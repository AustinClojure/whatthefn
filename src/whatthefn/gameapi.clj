(ns whatthefn.gameapi
  (:require [clojure.edn])
  (:use [whatthefn.messages :only [new-message!]]
        [whatthefn.events :only [write-to-game-channel!]]))

(defn username
  [req]
  (:username (:session req)))

(defn chat
  [{:keys [room-id player string username]}]
  (new-message! room-id
                {:type :chat
                 :player username
                 :string string}))

(defn resolve-input
  [{:keys [room-id player arg username]}]
  (write-to-game-channel! room-id
                          {:type :resolve-input
                           :player username
                           :arg (clojure.edn/read-string arg)
                           :room room-id}))

(defn test-solution
  [{:keys [room-id player function username]}]
  (write-to-game-channel! room-id
                          {:type :test-solution
                           :player username
                           :function function
                           :room room-id}))

(defn player-join-attempt
  [{:keys [room-id player username]}]
  (write-to-game-channel! room-id
                          {:type :player-join-attempt
                           :player username
                           :room room-id}))

(defn player-left
  [{:keys [room-id player username]}]
  (write-to-game-channel! room-id
                          {:type :player-left
                           :player username
                           :room room-id}))

(defn handler
  [req]
  (let [u (username req)]
    (case (:type (:params req))
      "chat" (chat (assoc (:params req) :username u))
      "resolve-input" (resolve-input (assoc  (:params req) :username u))
      "test-solution" (test-solution (assoc (:params req) :username u))
      "player-join-attempt" (player-join-attempt (assoc (:params req) :username u))
      "player-left" (player-left (assoc (:params req) :username u)))))
