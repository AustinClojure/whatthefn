(ns whatthefn.submit
  (:require [whatthefn.sandbox :as sb]
            [whatthefn.functions :as functions]
            [clojure.edn]))

(defn edn-response [data]
  {:body (pr-str data)
   :headers {"Content-Type" "application/edn;charset=UTF-8"}})

(defn str-reader [text]
  (java.io.PushbackReader. (java.io.StringReader. text)))

(defn read-one-edn [reader]
  (clojure.edn/read {:eof nil} reader))

(defn read-one [reader]
  (binding [*read-eval* false]
    (read reader false nil)))

(defn edn-seq [reader]
  (when-let [next-val (read-one reader)]
    (cons next-val (edn-seq reader))))


(defn eval-code [sandbox code]
  (let [forms (edn-seq (str-reader code))
        results (map sandbox forms)
        result-text (clojure.string/join "\n" (map pr-str results))]
    result-text))


(defn find-the-fn [sandbox]
  (try
    (sandbox 'the-fn)
    (catch Exception e
      nil)))

(defn test-the-fn [sandbox real-fn test-cases]
  (let [test-val (fn [val]
                   (try
                     (= (real-fn val)
                        (sandbox (list 'the-fn val)))
                     (catch Exception e
                       (println "eval error val " val)
                       false)))]
    (every? test-val test-cases)))

(defn submit-fn [code]
  (try
    (let [current-fn @functions/current-fn
          sandbox (sb/sandbox)
          eval-result (eval-code sandbox code)
          the-fn (find-the-fn sandbox)]
      (if the-fn
        (edn-response {:result (test-the-fn sandbox
                                            (functions/function-impl current-fn)
                                            (:tests current-fn))})
        (edn-response {:result "the-fn not found"})))

    (catch Exception e
      (edn-response {:result (.getMessage e)}))))


(defn submit-value [value-str]
  (let [sandbox (sb/sandbox)]
    (sandbox '(def the-fn (fn [x] (* x x))))

    (try
      (edn-response {:result (sandbox (list 'the-fn (clojure.edn/read-string value-str)))})
      (catch Exception e
        (edn-response {:result (.getMessage e)})))))


(defn submit-repl [req code]
  (let [session (:session req)
        repl (or (:repl session) (sb/sandbox))
        new-sesion (assoc session :repl repl)]
    (try
      (-> (edn-response {:result (eval-code repl code)})
          (assoc :session new-sesion))
      (catch Exception e
        (edn-response {:result (.getMessage e)})))))

(defn submit-fn-engine [code our-func callback orig]
  "test whether or not a function wins"
  (try
    (let [sandbox (sb/sandbox)
          eval-result (eval-code sandbox code)
          the-fn (find-the-fn sandbox)]
      (if the-fn
        {:type :function-eval-result
         :result (test-the-fn sandbox (functions/function-impl our-func) (:tests our-func))}
        (callback {:type :function-eval-result :result false :message "the-fn not found" :orig orig})))
    (catch Exception e
      (callback {:type :function-eval-result :result false :message (.getMessage e) :orig orig}))))

(defn submit-value-engine [value-str our-func callback]
  (prn value-str)
  (prn our-func)
  (prn callback)
  "get the output for a single input"
  (let [sandbox (sb/sandbox)]
    (sandbox (list ))
    (try
      (callback (sandbox (list 'our-func (clojure.edn/read-string value-str))))
      (catch Exception e
        (callback (.getMessage e))))))
