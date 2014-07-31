(ns data-cleanup.view
  (:use [hiccup core page form]
        [data-cleanup.connect :refer :all]))

(defn name-id-list
  ([list-body-output]
    (for [x list-body-output]
      (vector :p (vector :a {:href (str "/analyte/" (get x :id))} (get x :name))))))

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
  [request]
  (page-template
    "Select analyte"
    [:h1 "Select analyte"]
    (name-id-list (get-all-analytes))))

(defn foopage
  [request]
  (page-template
    "Foopage"
    [:h1 "Foopage"]
    [:p (str (:params request))]))

(defn barpage
  [request]
  (page-template
    "Barpage"
    [:h1 "Barpage"]
    [:p "Blank"]))
