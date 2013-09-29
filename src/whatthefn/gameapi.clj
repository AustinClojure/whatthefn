(ns whatthefn.gameapi
  (:require [clojure.edn])
  (:use [whatthefn.messages :only [new-message!]]
        [whatthefn.events :only [write-to-game-channel!]]))

(defn username
  [req]
  (:username (:session req)))

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
                           :arg (clojure.edn/read-string arg)
                           :room room-id}))

(defn test-solution
  [{:keys [room-id player function]}]
  (write-to-game-channel! room-id
                          {:type :test-solution
                           :player player
                           :function function
                           :room room-id}))

(defn player-join-attempt
  [{:keys [room-id player]}]
  (write-to-game-channel! room-id
                          {:type :player-join-attempt
                           :player player
                           :room room-id}))

(defn player-left
  [{:keys [room-id player]}]
  (write-to-game-channel! room-id
                          {:type :player-left
                           :player player
                           :room room-id}))

(defn handler
  [req]
  (case (:type (:params req))
    "chat" (chat (:params req))
    "resolve-input" (resolve-input (:params req))
    "test-solution" (test-solution (:params req))
    "player-join-attempt" (player-join-attempt (:params req))
    "player-left" (player-left (:params req))))
