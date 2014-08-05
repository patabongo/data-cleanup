(ns data-cleanup.view
  (:use [hiccup core page form]
        [data-cleanup.connect :refer :all]))

(defn name-id-list
  ([list-body-output category]
    (for [x list-body-output]
      (vector :p (vector :a {:href (str "/" category "/" (get x :id))} (get x :name))))))

(defn page-template
  [title header body]
  (html5
    [:head
     [:title title]]
    [:body
     [:a {:href "/"} "Home"]
     header
     body]))

(defn get-hostname
  [headers]
  (get headers "host"))

(defn analyte-page
  []
  (page-template
    "Select analyte"
    [:h1 "Select analyte"]
    (name-id-list (get-all-analytes) "analyte")))

(defn refcodes-page
  [analyte-id]
  (page-template
    "Select RefCode"
    [:h1 "Select RefCode"]
    (name-id-list (get-refcodes analyte-id) "design")))

(defn foopage
  [request]
  (page-template
    "Foopage"
    [:h1 "Foopage"]
    [:p (str (:params request))]))
