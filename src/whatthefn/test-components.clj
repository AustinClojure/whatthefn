(ns whatthefn.testfunctions)

(def pj {:type :player-join-attempt :player "newbie" :room :the-room})

(def pl {:type :player-left :player "newbie" :room :the-room})

(def pin {:type :resolve-input})

(def good-guess {:type :test-input})

(def bad-guess {:type :test-input})
