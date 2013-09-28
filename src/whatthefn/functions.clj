(ns whatthefn.functions)


(defn get-alpha[letter]
  "returns the index in the alphabet of the letter, i.e. A->1 (case insensitive)"
  (- (int (clojure.string/upper-case letter)) 64))


(def function-bases
  {'("")})

(defn build-function [fname type id body desc]
  {:name fname :type type :id id :body body :description desc})

(defn build-all-functions [bases]
  (let []))

(def function-map (build-all-functions function-bases))

(defn get-next-function [seen-functions]
  (function-map (rand-int (count function-map))))
