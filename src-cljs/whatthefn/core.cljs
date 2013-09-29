(ns whatthefn.core
  (:require [ajax.core :as ajax]
            [dommy.core :as dommy]
            [clojure.string :as string]
            [whatthefn.repl :as repl]
            [whatthefn.chat :as chat])  
  (:use-macros [dommy.macros :only [sel sel1]]))

;; ----------------------------------------
(defn out-field [resp]
  (.log js/console "out-field: " (pr-str (:result resp)))
  (.testSolution js/api (pr-str (:result resp)))
  
  (comment  (-> (sel1 :#statusbox)
                (dommy/append! (str "\nResult: " (pr-str (:result resp)))))))


(defn send-fn [fn-text]
  (ajax/POST "/submit-fn"
             {:params {:code fn-text}
              :format :edn
              :handler out-field}))


(defn submit-clicked []
  (.testSolution js/api (.getValue js/editor)))

(defn set-session-id []
  (.log js/console js/api)
  (let [user (.username js/api)]
    (-> (sel1 :#session_button)
        (dommy/set-text! (str " Sign Out: " user)))))

;; ----------------------------------------

(defn ^:external init []
  (.log js/console "starting")
  (-> (sel1 :#submitbutton)
      (dommy/listen! :click submit-clicked))
  (repl/init-repl)
  (chat/init-chat)

  (.log js/console "started"))
