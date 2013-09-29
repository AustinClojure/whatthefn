(ns whatthefn.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [cheshire.core :as json]
            [ring.middleware.edn :as edn-params]
            [ring.middleware.session.memory :as mem]
            [ring.util.response :as response]
            [whatthefn.auth :as auth]
            [whatthefn.messages :as messages]
            [whatthefn.gameapi :as gameapi]
            [whatthefn.submit]))

(defn json-response [data]
  {:body (json/encode data)
   :headers {"Content-Type" "application/json;charset=UTF-8"}})

(defn edn-response [data]
  {:body (pr-str data)
   :headers {"Content-Type" "application/edn;charset=UTF-8"}})

(defn static-file [file]
  (response/file-response file {:root "resources/public"}))

(defroutes no-auth-routes
  (GET "/" req
       (if (auth/logged-in? req)
         (response/redirect "/app")
         (static-file "/index.html")))

  (POST "/login" [username password :as req]
        (auth/login req username password))
  (POST "/logout" []
        (auth/logout))
  (POST "/register" [username password :as req]
        (auth/register req username password))

  (route/resources "/")
  (GET "/session" [:as req] {:body (pr-str (:session req))}))

(defroutes auth-routes
  (GET "/app" [] (static-file "/app.html") )

  (POST "/submit-fn" [code]
        (whatthefn.submit/submit-fn code))
  (POST "/submit-repl" [code :as req]
        (whatthefn.submit/submit-repl req code))
  (GET "/submit-value" [value]
       (whatthefn.submit/submit-value value))

  (GET "/rooms/:room-id/messages" {{room-id :room-id since :since} :params}
       (comp json-response messages/get-messages))
  (POST "/rooms/:room-id/messages" {{room-id :room-id message :message} :params}
        (json-response (messages/new-message! room-id {:str message})))
  (POST "/rooms/:room-id/events" {{room-id :room-id message :message} :params}
        (comp json-response gameapi/handler))

  (route/not-found "Not Found"))

(defroutes app-routes
  no-auth-routes
  (auth/wrap-require-user auth-routes))

(defonce session-atom (atom {}))
(defonce session (mem/memory-store session-atom))

(def app
  (-> app-routes
      (edn-params/wrap-edn-params)
      (handler/site {:session {:store session}})))
