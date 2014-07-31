(ns data-cleanup.view
  (:use [hiccup core page form]
        [data-cleanup.connect :refer :all]))

(defn name-id-list
  ([list-body-output]
    (for [x list-body-output]
      (vector :p (vector :a {:href (str "/analyte/" (get x :id))} (get x :name))))))

(defn page-template
  [title navigation header body]
  (html5
    [:head
     [:title title]]
    [:body
     navigation
     header
     body]))

(defn get-hostname
  [headers]
  (get headers "host"))

(defn analyte-page
  [request]
  (page-template
    "Select analyte"
    [:a {:href (str "http://" (get-hostname (:headers request)))} "Home"]
    [:h1 "Select analyte"]
    (name-id-list (get-all-analytes))))

(defn foopage
  [request]
  (page-template
    "Foopage"
    [:a {:href (str "http://" (get-hostname (:headers request)))} "Home"]
    [:h1 "Foopage"]
    [:p (str (:params request))]))

(defn barpage
  [request]
  (page-template
    "Barpage"
    [:a {:href (str "http://" (get-hostname (:headers request)))} "Home"]
    [:h1 "Barpage"]
    [:p "Blank"]))
