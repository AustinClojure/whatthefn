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

(def function-bases
  #{'("a friend of lucas" :numeral fib "our first function" #{2 5 10})
    '("a prime problem" :numeral prime-factors "second function" #{7 9 24 12 30})
    '("what's the opposite of gestalt?" :numeral sum-prime-factors "third function" #{7 9 8 24 12 30})
    '("a matter of fact" :numeral factorial "4th function" #{2 5 3})})

(defn build-function [id [fname type body desc tests]]
  {:name fname :type type :id id :body body :description desc :tests tests})

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

(defn get-function [id]
  (function-map id))

(defn eval-function [id arg]
  "returns the result of function <id> evaluated with argument <arg>"
  ((get-function id) arg))

(defn test-function [id f]
  "returns true if the user function, <f> provides the same output as the game's function referred to by<id>, false otherwise"
  (let [against (get-function id)
        tests (:tests against)
        func (:body against)]
    ((every? true? (for [test tests]
                     (= (f test) (against test)))))))
