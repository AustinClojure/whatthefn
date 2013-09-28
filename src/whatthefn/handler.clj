(ns whatthefn.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [hiccup.page :as page]
            [hiccup.element :as elem]
            [ring.middleware.edn :as edn-params]
            [ring.util.response :as response]
            [whatthefn.submit]))


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
          [:textarea#fnin {:type :textarea}]
          [:br]
          [:input#fnout {:type :text}]
          [:button#submittest "Test"]
          (page/include-js "/js/wtfn.js")
          (elem/javascript-tag "whatthefn.core.init();")))


(defroutes app-routes
  (GET "/" [] (response/redirect "/app.html"))

  (POST "/submit-fn" [code] (whatthefn.submit/submit-fn code))
  (POST "/submit-repl" [code] (whatthefn.submit/submit-repl code))
  (GET "/submit-value" [value] (whatthefn.submit/submit-value value))

  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> app-routes
      edn-params/wrap-edn-params
      handler/site))
