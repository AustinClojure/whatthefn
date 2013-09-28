(ns whatthefn.core
  (:require [ajax.core :as ajax]
            [dommy.core :as dommy])
  (:use-macros [dommy.macros :only [sel sel1]]))

(defn log-response [resp]
  (.log js/console "RESP" (pr-str resp)))

(defn out-field [resp]
  (.log js/console "RESP!" (pr-str (:result resp)))
  (-> (sel1 :#fnout)
      (dommy/set-value! (pr-str (:result resp)))))

(defn send-fn [fn-text]
  (ajax/POST "/submit-fn"
             {:params {:code fn-text}
              :format :edn
              :handler out-field}))

(defn submit-clicked []
  (send-fn (dommy/value (sel1 :#fnin))))

(defn ^:external init []
  (.log js/console "starting")
  (-> (sel1 :#submittest)
      (dommy/listen! :click submit-clicked)))
