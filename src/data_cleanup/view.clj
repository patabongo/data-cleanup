(ns data-cleanup.view
  (:use [hiccup core page form]
        [data-cleanup.connect :refer :all]))

(defn name-id-list
  ([list-body-output category]
    (for [x list-body-output]
      (vector :p (vector :a {:href (str "/" category "/" (get x :id))} (get x :name))))))

(defn last-panel-nav
  [design-id]
  (let [x (first (get-last-panel design-id))]
    [:a {:href (str (get x :designid))} (str "&lt;&lt;" (get x :refcode))]))

(defn next-panel-nav
  [design-id]
  (let [x (first (get-next-panel design-id))]
    [:a {:href (str (get x :designid))} (str (get x :refcode) "&gt;&gt;")]))

(defn map-tag
     [tag xs]
     (map (fn [x] [tag x]) xs))

(defn field-keywords-to-names
  [result-set]
  (cons 
    (vec (map  #(vector :b (name %)) (first result-set))) 
    (rest result-set)))

(defn results-to-table
  [namified-results]
  (map (fn [x] (vec (cons :tr (into [] x)))) (map #(map-tag :td %) namified-results)))

(defn page-template
  [title header body]
  (html5
    [:head
     (include-css "/css/site.css")
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
  [refcodes]
  (page-template
    "Select RefCode"
    [:h1 "Select RefCode"]
    (name-id-list refcodes "design")))

(defn enter-regex-page
  [analyte-id]
  (html5 (form-to [:post "/regex/"]
                  (hidden-field :analyte-id analyte-id)
                  (label "input" "Enter a Regex to search programme RefCodes:")
                  [:br]
                  (text-field "regex")
                  [:br]
                  (submit-button "Search!"))))

(defn refcodes-switch
  [analyte-id]
  (let [x (get-refcodes analyte-id)]
    (if (empty? x)
      (enter-regex-page analyte-id)
      (refcodes-page x))))

(defn panel-contents-page
  [design-id]
  (page-template
    "Panel contents"
    (list 
      [:p (last-panel-nav design-id)]
      [:h1 (get-refcode-from-design design-id)]
      [:p (next-panel-nav design-id)])
    [:table (results-to-table
     (field-keywords-to-names
       (get-panel-contents design-id)))]))

(defn foopage
  [request]
  (page-template
    "Foopage"
    [:h1 "Foopage"]
    [:p (str (:params request))]))
