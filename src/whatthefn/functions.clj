(ns whatthefn.functions)

(defn fib [n]
  (cond
    (<= n 0) 0
    (= n 1) 1
    :else
    (loop [count 0
           prev 1
           prevprev 0]
      (if (= count n)
        prev
        (recur (inc count) (+ prev prevprev) prev)))))

(defn get-alpha[letter]
  "returns the index in the alphabet of the letter, i.e. A->1 (case insensitive)"
  (- (int (clojure.string/upper-case letter)) 64))

(defn get-letter [alpha]
  "returns the letter associated with this alpha in ASCII"
  (let [shifted (+ alpha 64)]
    (char shifted)))

(def function-bases
 #{'("a friend of lucas" :numeral fib "our first function")})

(defn build-function [id [fname type body desc]]
  {:name fname :type type :id id :body body :description desc})

(defn build-all-functions [bases]
  (loop [x 0
         bs bases
         acc {}]
    (if (empty? bs)
      acc
      (recur (inc x) (rest bases) (assoc acc x (build-function x (first bases)))))))

(def function-map (build-all-functions function-bases))

(defn get-next-function
  ([seen-functions] (function-map (rand-int (count function-map))))
  ([] (get-next-function '())))
