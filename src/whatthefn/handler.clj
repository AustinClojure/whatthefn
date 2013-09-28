(ns whatthefn.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [hiccup.page :as page]
            [hiccup.element :as elem]
            [ring.middleware.edn :as edn-params]))


(defn test-edn []
  {:body (pr-str {:this :is :a :test})
   :headers {"Content-Type" "application/edn;charset=UTF-8"}})

(defn layout [title & body]
  (page/html5
   [:head [:title title]]
   [:body body]))

(defn test-page []
  (layout "What The FN"
          [:h1 "What the FN test page"]
          [:p "This is a quick example"]
          (page/include-js "/js/wtfn.js")
          (elem/javascript-tag "whatthefn.core.init();")))


(defroutes app-routes
  (GET "/" [] (test-page))

  (route/resources "/")
  (GET "/edn-test" [] (test-edn))
  (route/not-found "Not Found"))

(def app
  (-> app-routes
      edn-params/wrap-edn-params
      handler/site))
