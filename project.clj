(defproject whatthefn "0.1.0-SNAPSHOT"
  :description "WHAT THE FN?"
  :url "http://clojurecup.com/app.html?app=whatthefn"

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.5"]
                 [ring-edn "0.1.0"]
                 [clojail "1.0.6"]]

  :plugins [[lein-ring "0.8.5"]]

  :ring {:handler whatthefn.handler/app
         :nrepl {:start? true
                 :port 3030}}

  :profiles
  {:dev {:dependencies [[ring-mock "0.1.5"]]}}

  :jvm-opts ["-Djava.security.policy=whatthefn.policy"])
