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

(defn prime-factors [n]
  (loop [acc #{}
         t 2
         rem n]
    (if (> (* t t) rem)
      (conj acc rem)
      (if (= 0 (mod rem t))
        (recur (conj acc t) 2 (/ rem t))
        (recur acc (inc t) rem)))))

(defn sum-prime-factors [n]
  (reduce + (prime-factors n)))

(defn factorial [n]
  (loop [rem n
         tot 1]
    (if (<= rem 0)
      tot
      (recur (dec rem) (* tot rem)))))

(defn get-alpha[letter]
  "returns the index in the alphabet of the letter, i.e. A->1 (case insensitive)"
  (- (int (clojure.string/upper-case letter)) 64))

(defn get-letter [alpha]
  "returns the letter associated with this alpha in ASCII"
  (let [shifted (+ alpha 64)]
    (char shifted)))

(defn our-name [n]
  (let [nm "what the fn!"]
    (try (nth nm n)
         (catch Exception e
           nil))))

(defn follow-the [n]
  (let [our-map {1 99 99 200 200 700 700 909 909 121212 121212 1}]
    (our-map n)))

(defn prime? [n]
  (let [pf (prime-factors n)]
    (and (= (count pf) 1) (not= n 1))))

(defn even-only [n]
  (filter even? n))

(def function-bases
  #{["a friend of lucas" :numeric fib "our first function" #{2 5 10}]
    ["a prime problem" :numeric prime-factors "second function" #{7 9 24 12 30}]
    ["what's the opposite of gestalt?" :numeric sum-prime-factors "third function" #{7 9 8 24 12 30}]
    ["a matter of fact" :numeric factorial "4th function" #{2 5 3}]
    ["under the bridge" :numeric #(count (str %)) "5th function" #{2345 2 22 213092310}]
    ["singular primes" :numeric #(reduce * (prime-factors %)) "6th function" #{3 2 4 6}]
    ["what the fn!" :numeric our-name "7th function" #{0 1 2 3 4 5 6 7 8 9 10 11 12}]
    ["follow the 1 through the trail!!!" :numeric follow-the #{1 99 200 700}]
    ["prime?" :numeric prime? #{1 2 16 7 23 8}]
    ["the even part" :numeric-collection even-only #{#{1 2 3 4} #{3 4 5 19 20 1}}]})

(defn plus2 [n]
  (+ n 2))

(defn times3 [n]
  (* 3 n))

(def function-bases-unused
  #{["+ 2" :numeral plus2 "test me" #{2 4 100}]
    ["* 3" :numeral times3 "test me" #{2 4 202}]})

(defn build-function [id [fname type body desc tests]]
  {:name fname :type type :id id :body body :description desc :tests tests})

(defn build-all-functions [bases]
  (zipmap (range)
          (map build-function (range) bases)))

(def function-map (build-all-functions function-bases))

(defn get-next-function
  ([seen-functions] (function-map (rand-int (count function-map))))
  ([] (get-next-function '())))

(defn function-impl [function-def]
  (ns-resolve (find-ns 'whatthefn.functions) (:body function-def)))

(defonce current-fn (atom (get-next-function)))

(defn test-fun [id arg callback]
  (try
    (let [fun (:body (function-map id))]
      (prn "test-fun" (fun arg))
      (prn "arg" arg)
      (callback (fun arg)))
    (catch Exception e
      (callback (.getMessage e)))))
