(ns whatthefn.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [hiccup.page :as page]
            [hiccup.element :as elem]
            [ring.middleware.edn :as edn-params]
            [ring.middleware.session.memory :as mem]
            [ring.util.response :as response]
            [whatthefn.messages :as messages]
            [whatthefn.submit]))

(defn edn-response [data]
  {:body (pr-str data)
   :headers {"Content-Type" "application/edn;charset=UTF-8"}})

(defn layout [title & body]
  (page/html5
   [:head [:title title]]
   [:body body]))


(defn counter [req]
  (println (:session req))
  (let [safe-inc (fnil inc 0)
        new-val (safe-inc (get-in req [:session :counter]))]
    {:body (str new-val)
     :session {:counter new-val}}))


(defroutes app-routes
  (GET "/" [] (response/redirect "/app.html"))

  (POST "/submit-fn" [code]
        (whatthefn.submit/submit-fn code))
  (POST "/submit-repl" [code]
        (whatthefn.submit/submit-repl code))
  (GET "/submit-value" [value]
       (whatthefn.submit/submit-value value))

  (GET "/rooms/:room-id/messages" {{room-id :room-id since :since} :params}
       (edn-response (messages/messages-since room-id since)))
  (POST "/rooms/:room-id/messages" {{room-id :room-id message :message} :params}
        (edn-response (messages/new-message! room-id {:str message})))

  (GET "/counter" [] counter)

  (route/resources "/")
  (route/not-found "Not Found"))



(def app
  (-> app-routes
      (edn-params/wrap-edn-params)
      (handler/site
       {:store (mem/memory-store)})))
