(ns whatthefn.core)

(def message "this is a cljs var")

(defn ^:external init []
  (.log js/console "starting"))
