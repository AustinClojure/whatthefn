(ns whatthefn.sandbox
  (:require [clojail.core :as clojail]
            [clojail.testers :as testers]))

(defn sandbox []
  (clojail/sandbox testers/secure-tester-without-def))



