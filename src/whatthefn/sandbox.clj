(ns whatthefn.sandbox
  (:require [clojail.core :as clojail]
            [clojail.testers :as testers]))

(def restricted {:symbol
                 '[use require ns in-ns future agent send send-off pmap pcalls]
                 :ns
                 '[whatthefn compojure cheshire ring clojure.core.async clj-time]
                 :package
                 []
                 :object
                 []})

(defn wtfn-tester []
  (conj testers/secure-tester-without-def
        (testers/blacklist-symbols (:symbol restricted))
        (testers/blacklist-nses (:ns restricted))
        (testers/blacklist-packages (:package restricted))
        (testers/blacklist-objects (:object restricted))))

(defn sandbox []
  (clojail/sandbox (wtfn-tester) :timeout 1000))



