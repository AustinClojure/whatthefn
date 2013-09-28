(ns whatthefn.core
  (:require [ajax.core :as ajax]
            [dommy.core :as dommy]
            [clojure.string :as string])
  (:use-macros [dommy.macros :only [sel sel1]]))


(defn out-field [resp]
  (.log js/console "out-field: " (pr-str (:result resp)))
  (-> (sel1 :#statusbox)
      (dommy/append! (str "\nResult: " (pr-str (:result resp))))))

(defn out-repl [resp]
  (.log js/console "out-repl: " (pr-str (:result resp)))
  (let [old-val (dommy/value (sel1 :#repl))])
   (dommy/set-value! (sel1 :#repl) (str  old-val (pr-str (:result resp))) "\n"))


(defn send-fn [fn-text]
  (ajax/POST "/submit-fn"
             {:params {:code fn-text}
              :format :edn
              :handler out-field}))

(defn send-repl [fn-text]
  (ajax/POST "/submit-repl"
             {:params {:code fn-text}
              :format :edn
              :handler out-repl}))


(defn submit-clicked []
  (send-fn (.getValue js/editor)))

(defn key-pressed [e]
  (if (= 13 (.-which e))
     (send-repl (-> (sel1 :#repl)
              (.-value)
               (string/split #"\n")
               (last)))))


(defn ^:external init []
  (.log js/console "starting")
  (-> (sel1 :#submitbutton)
      (dommy/listen! :click submit-clicked))
  (-> (sel1 :#repl)
      (dommy/listen! :keypress key-pressed)))
