(defproject whatthefn "0.1.0-SNAPSHOT"
  :description "WHAT THE FN?"
  :url "http://clojurecup.com/app.html?app=whatthefn"

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.5"]

                 [ring-edn "0.1.0"]
                 [clojail "1.0.6"]
                 [cheshire "5.2.0"]
                 [clj-time "0.6.0"]
                 [org.clojure/core.async "0.1.222.0-83d0c2-alpha"]

                 [org.clojure/clojurescript "0.0-1877"]
                 [cljs-ajax "0.2.0"]
                 [prismatic/dommy "0.1.2"]]

  :plugins [[lein-ring "0.8.5"]
            [lein-cljsbuild "0.3.3"]]

  :hooks [leiningen.cljsbuild]
  :cljsbuild {:builds [{:source-paths ["src-cljs"]
                        :compiler {:output-to "resources/public/js/wtfn.js"
                                   :optimizations :whitespace
                                   :pretty-print true}}]}

  :ring {:handler whatthefn.handler/app
         :nrepl {:start? true
                 :port 3030}}

  :profiles
  {:dev {:dependencies [[ring-mock "0.1.5"]]}}

  :jvm-opts ["-Djava.security.policy=whatthefn.policy"])
