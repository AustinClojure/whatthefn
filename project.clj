(defproject whatthefn "0.1.0-SNAPSHOT"
  :description "WHAT THE FN?"
  :url "http://clojurecup.com/app.html?app=whatthefn"

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.5"]
                 [hiccup "1.0.4"]
                 [ring-edn "0.1.0"]
                 [clojail "1.0.6"]
                 [org.clojure/clojurescript "0.0-1877"]]

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
