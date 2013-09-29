(ns whatthefn.events
  (:require [clojure.core.async :refer :all]))

(def game-channels (ref {}))

(defn new-game!
  [game-id]
  (alter game-channels assoc game-id (chan 10)))

(defn start-dummy-writer
  [game-id]
  (let [channel (@game-channels game-id)]
    (.start (java.lang.Thread. #(doseq [n (range)]
                                  (go (>!! channel {:type :tick
                                                    :i-am-an :event
                                                    :param n}))
                                  (Thread/sleep 5000)
                                  )))))

(defn ensure-game-exists
  [game-id]
  (dosync
    (when-not (@game-channels game-id)
      (new-game! game-id)
      (start-dummy-writer game-id))))

(defn game-channel
  [game-id]
  (ensure-game-exists game-id)
  (@game-channels game-id))

(defn write-to-game-channel!
  [game-id event]
  (>!! (game-channel game-id) event))

(defn example
  []
  (let [game-channel (game-channel :my-game-id)
        state-transition-function (fn [state event] state)]
    (go
      (loop [game-state {:starting :game-state}]
        (let [next-event (<!! game-channel)]
          (prn next-event)
          ;write broadcast message here
          (recur (state-transition-function game-state next-event)))))))
