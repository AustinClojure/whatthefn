(ns whatthefn.error
  (:require [hiccup.core :as hiccup]
            [hiccup.page :as page]
            [hiccup.element :as elem]))



(defn error-page [status message]
  {:status status
   :body  (page/html5
           [:head [:title "WTF?!?!?!?"]
            (page/include-css "css/bootstrap-responsive.css"
                              "css/style.css")]
           [:body
            [:div.hero-unit.center
             [:h1 (hiccup/h message)]
             [:p "Sorry, there seems to be a problem.  Stay calm, and keep on coding!"]
            [:a.btn.btn-large.btn-info {:href "/"}
             [:i.icon-home.icon-white] "&nbsp;Home"]]])})

