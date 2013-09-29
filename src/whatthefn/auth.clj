(ns whatthefn.auth
  (:require [ring.util.response :as response]
            [whatthefn.error :as err]))

;;----------------------------------------

(defn logged-in? [req]
  (get-in req [:session :username]))

(defn wrap-require-user [handler]
  (fn [req]
    (if (logged-in? req)
      (handler req)
      (err/error-page 401 "Not Authenticated"))))

;; ----------------------------------------
(defonce user-store (atom {}))

(defn valid-login? [username password]
  (let [expected-password (get @user-store username)]
    (and (not (empty? expected-password))
         (= password expected-password)
         true)))

(defn add-user-password [username password]
  (swap! user-store
         (fn [store]
           (if (contains? store username)
             store
             (assoc store username password)))))

;; ----------------------------------------

(defn login [req username password]
  (if (valid-login? username password)
    (assoc (response/redirect "/app")
      :session (assoc (:sesssion req) :username username))
    (response/redirect "/")))

(defn logout []
  (assoc (response/redirect "/")
    :session {}))


(defn register [req username password]
  (add-user-password username password)
  (login req username password))
