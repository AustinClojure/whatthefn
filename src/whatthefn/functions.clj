(ns whatthefn.functions)

(defn get-alpha[letter]
  (- (int (clojure.string/upper-case letter)) 64))

(def function-bases)

(defn build-function [(fname type id body desc)]
  {:name fname :type type :id id :body body :description desc})

(defn build-all-functions [bases])

(defn get-next-function [seen-functions]
  ;;
  )
