(ns whatthefn.repl
  (:require [ajax.core :as ajax]
            [dommy.core :as dommy]
            [clojure.string :as string])
  (:use-macros [dommy.macros :only [sel sel1]]))

(def cursor-pos (atom 0))

(defn update-cursor []
  (reset! cursor-pos
          (count (-> (sel1 :#repl)
                     (dommy/value)))))
(defn repl-output [text]
  (let [repl (sel1 :#repl)]
    (dommy/set-value! repl
     (str (dommy/value repl)
          "\n"
          text
          "\n> ")))
  (update-cursor))

(defn extract-repl-command []
  (-> (sel1 :#repl)
      (dommy/value)
      (subs @cursor-pos)))


(defn out-repl [resp]
  (.log js/console "out-repl: " (pr-str (:result resp)))
  (repl-output (:result resp)))



(defn send-repl [fn-text]
  (ajax/POST "/submit-repl"
             {:params {:code fn-text}
              :format :edn
              :handler (fn [resp] (repl-output (:result resp)))}))

(defn key-pressed [e]
  (when (= 13 (.-which e))
    (send-repl (extract-repl-command))))


;; ----------------------------------------
(defn init-repl []
  (-> (sel1 :#repl)
      (dommy/listen! :keypress key-pressed))

  (repl-output ";; WhatTheFn REPL"))
