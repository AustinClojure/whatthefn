(ns whatthefn.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.middleware.edn :as edn-params]))


(defn test-edn []
  {:body (pr-str {:this :is :a :test})
   :headers {"Content-Type" "application/edn;charset=UTF-8"}})

(defroutes app-routes
  (GET "/" [] "Welcome to What The FN??!?!")
  (route/resources "/")
  (GET "/edn-test" [] (test-edn))
  (route/not-found "Not Found"))

(def app
  (-> app-routes
      edn-params/wrap-edn-params
      handler/site))
