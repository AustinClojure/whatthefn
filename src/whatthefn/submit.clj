(ns whatthefn.submit
  (:require [whatthefn.sandbox :as sb]
            [clojure.edn]))

(defn edn-response [data]
  {:body (pr-str data)
   :headers {"Content-Type" "application/edn;charset=UTF-8"}})

(defn submit-fn [code]
  (let [result  ((sb/sandbox) (clojure.edn/read-string code))]
    (edn-response {:result result})))
