(ns data-cleanup.view
  (:use [hiccup core page form]
        [data-cleanup.connect :refer :all]
        [ring.util.response :only [redirect]]))

(defn name-id-list
  ([list-body-output category]
    (for [x list-body-output]
      (vector :p (vector :a {:href (str "/data/" category "/" (get x :id))} (get x :name))))))

(defn last-panel-nav
  [design-id]
  (let [x (first (get-last-panel design-id))]
    [:a {:href (str "/data/design/" (get x :designid))} (str "&lt;&lt;" (get x :refcode))]))

(defn next-panel-nav
  [design-id]
  (let [x (first (get-next-panel design-id))]
    [:a {:href (str "/data/design/" (get x :designid))} (str (get x :refcode) "&gt;&gt;")]))

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

(defn linked-first-vector
  [result-set]
  (cons
    (rest (first result-set))
    (into [] (map #(vector [:a {:href (str "/data/design/" (first %))} (second %)] (nth % 2) (nth % 3)) (rest result-set)))))

(defn tableify
  [arrs]
  [:table (results-to-table
            (field-keywords-to-names
              arrs))])

(defn page-template
  [title header body]
  (html5
    [:head
     (include-css "/css/site.css")
     [:title title]]
    [:body
     [:p [:a {:href "/"} "Home"] " &gt; " [:a {:href "/data/"} "Data cleanup"]]
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

(defn refcodes-page-back
  [refcodes]
  (page-template
    "Select RefCode"
    [:h1 "Select RefCode"]
    (name-id-list refcodes "design")))

(defn refcodes-page
  [result-set]
  (page-template
    "Select RefCode"
    [:h1 "Select RefCode"]
    (tableify (linked-first-vector result-set))))

(defn check-regex-page
  [regex analyte-id]
  (page-template
    "Check regex"
    [:h1 "Check Regex"]
    (list
      [:p (str "The regex you entered was: " regex)]
      [:table (results-to-table
     (field-keywords-to-names
       (get-programmes-from-regex regex)))]
      [:p "If this list looks incomplete, " [:a {:href (str "/data/analyte/" analyte-id)} "click here"] " to edit the regex."]
      [:p "Otherwise, "]
      (form-to [:post "/data/submit/"]
                (hidden-field :regex regex)
                (hidden-field :analyte-id analyte-id)
                (submit-button "Submit!")))))
      

(defn enter-regex-page
  [analyte-id]
  (page-template
    "Enter regex"
    [:h1 "Regex matching"]
    (form-to [:post "/data/regex/"]
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
      (refcodes-page (get-refcodes-with-pairs analyte-id)))))

(defn submit-regex
  [regex analyte-id]
  (do 
    (commit-regex-to-db regex analyte-id)
    (refcodes-page (get-refcodes analyte-id))))

(defn panel-contents-page
  [design-id]
  (page-template
    "Panel contents"
    (list 
      [:p (last-panel-nav design-id)]
      [:h1 (get-refcode-from-design design-id)]
      [:p (next-panel-nav design-id)])
    (list (tableify (get-panel-contents design-id))
   (let [pairs (get-sample-pairs design-id)]
     (if (empty? (rest pairs))
                 (str "\n")
                 (list [:h2 "Currently identified sample pairs:"]
                       (tableify pairs))))
   [:h2 "Register a new set of paired samples:"]
   (form-to [:post "/data/pairsave/"]
             (hidden-field :design-id design-id)
             (label "samplecode1" "Sample 1: ") (drop-down :samplecode1 (map #(vector % %) (get-sample-codes design-id)))
             (label "samplecode2" " Sample 2: ") (drop-down :samplecode2 (map #(vector % %) (get-sample-codes design-id)))
             [:br]
             (label "pairtype" "Pair type: ") (drop-down :pairtype (map #(vector % %) ["Duplicate" "Dilution"]))
             [:br]
             (label "paircontents" "Contents: ") (drop-down :paircontents (map #(vector % %) (get-sample-contents design-id)))
             (submit-button "Confirm!")))))

(defn pair-save
  [params]
  (do
    (save-sample-pair params)
    (redirect (str "/data/design/" (get params :design-id)))))

(defn foopage
  [params]
  (page-template
    "Foopage"
    [:h1 "Foopage"]
    [:p (str  params)]))
