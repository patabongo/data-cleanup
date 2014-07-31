(ns data-cleanup.view
  (:use [hiccup core page form]
        [data-cleanup.connect :refer :all]))

(defn name-id-list
  ([list-body-output category]
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
  [my-req]
  (get-in my-req [:headers "host"]))

(defn foopage
  [request]
  (page-template
    "Foopage"
    [:a {:href (str "http://" (get-hostname request))} "Home"]
    [:h1 "Foopage"]
    [:p "blank"]))

(defn barpage
  [request]
  (page-template
    "Barpage"
    [:a {:href (str "http://" (get-hostname request))} "Home"]
    [:h1 "Barpage"]
    [:p "Blank"]))
