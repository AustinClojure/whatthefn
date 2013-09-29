(ns whatthefn.gameapi
  (:require [clojure.edn]
            [whatthefn.engine :as engine])
  (:use [whatthefn.messages :only [new-message!]]
        [whatthefn.events :only [write-to-game-channel! assert-engine-on-room]]))

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

(defn function-eval-result
  [{:keys [room-id player function orig func-id result]}]
  (write-to-game-channel! room-id
                          {:type :function-eval-result
                           :result result
                           :orig orig
                           :room room-id
                           :player player
                           :func-id func-id
                           :function function}))

(defn handler
  [req]
  (let [u (username req)]
    (assert-engine-on-room (:room-id (:params req)) engine/start-engine)
    (case (:type (:params req))
      "chat" (chat (assoc (:params req) :username u))
      "resolve-input" (resolve-input (assoc  (:params req) :username u))
      "test-solution" (test-solution (assoc (:params req) :username u))
      "player-join-attempt" (player-join-attempt (assoc (:params req) :username u))
      "player-left" (player-left (assoc (:params req) :username u)))))
