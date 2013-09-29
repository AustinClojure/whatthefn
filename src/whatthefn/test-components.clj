(ns whatthefn.testfunctions)

(def pj {:type :player-join-attempt :player "newbie" :room :the-room})

(def pl {:type :player-left :player "newbie" :room :the-room})

(defn pin [arg]
  {:type :resolve-input
   :room :the-room
   :arg arg})

(def good-guess {:type :test-input
                 :room :the-room
                 :player "newbie"
                 :function #(* % 2)})

(def bad-guess {:type :test-input
                :room :the-room
                :player "newbie"
                :function #(+ % 2)})
