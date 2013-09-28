(ns whatthefn.test.sandbox
  (:use clojure.test
        ring.mock.request
        whatthefn.sandbox))

(deftest test-sandbox
  (testing "test sandbox can eval"
    (let [sb (sandbox) ]
      (is (= 3 (sb '(+ 1 2)))))))
