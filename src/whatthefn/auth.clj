(ns whatthefn.auth
  (:require [ring.util.response :as response]))

;;----------------------------------------

(defn wrap-require-user [handler]
  (fn [req]
    (if (get-in req [:session :username])
      (handler req)
      {:status 401 :body "NOT AUTHENTICATED"})))

;; ----------------------------------------
(defn valid-login? [username password]
  (println "LOGIN" username password)
  true)

(defn login [req username password]
  (if (valid-login? username password)
    (assoc (response/redirect "/app")
      :session (assoc (:sesssion req) :username username))
    (response/redirect "/")))

(defn logout []
  (assoc (response/redirect "/")
    :session {}))
