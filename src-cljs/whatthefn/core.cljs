(ns whatthefn.core
  (:require [ajax.core :as ajax]
            [dommy.core :as dommy])
  (:use-macros [dommy.macros :only [sel sel1]]))

(defn log-response [resp]
  (.log js/console "RESP" (pr-str resp)))

(defn out-field [resp]
  (.log js/console "RESP!" (pr-str (:result resp)))
  (-> (sel1 :#statusbox)
      (dommy/append! (str "Result: " (pr-str (:result resp))))))

(defn send-fn [fn-text]
  (ajax/POST "/submit-fn"
             {:params {:code fn-text}
              :format :edn
              :handler out-field}))

(defn submit-clicked []
  (send-fn (.getValue js/editor)))

(defn ^:external init []
  (.log js/console "starting")
  (-> (sel1 :#submitbutton)
      (dommy/listen! :click submit-clicked)))
