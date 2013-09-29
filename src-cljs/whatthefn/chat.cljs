(ns whatthefn.chat
  (:require [ajax.core :as ajax]
            [dommy.core :as dommy]
            [clojure.string :as string])  
  (:use-macros [dommy.macros :only [sel sel1]]))

;; ----------------------------------------
(defn clear-old-message []
  (-> (sel1 :#chat_input_box)
      (dommy/set-value! "")))

(defn out-chat [msg]
  (.chat js/api (str msg))
  (clear-old-message))

(comment
  (defn send-chat [fn-text]
    (ajax/POST "/send-chat"
        {:params {:code fn-text}
         :format :edn
         :handler out-chat})))

(defn extract-chat-message []
  (-> (sel1 :#chat_input_box)
      (dommy/value)))

(defn key-pressed [e]
  (when (= 13 (.-which e))
    (out-chat (extract-chat-message))))

;; ----------------------------------------

(defn init-chat []
  (-> (sel1 :#chat_input_box)
      (dommy/listen! :keypress key-pressed))) 
