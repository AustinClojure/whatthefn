(ns whatthefn.auth
  (:require [ring.util.response :as response]))


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
