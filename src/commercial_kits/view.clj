(ns commercial-kits.view
  (:use [hiccup core page form]
        [commercial-kits.connect :refer :all]))

(defn name-id-list
  ([list-body-output category]
    (for [x list-body-output]
      (vector :p (vector :a {:href (str "/kits/kitlist?by=" category "&id=" (get x :id))} (get x :name))))))

(defn kit-list
  [list-body-output]
  (for [x list-body-output]
        (vector :p (vector :a {:href (str "/kits/kit/" (get x :id))} (get x :name)))))

(defn page-template
  [title navigation header body]
  (html5
    [:head
     [:title title]]
    [:body
     navigation
     header
     body]))

(defn index-page
  []
  (page-template
    "Commercial kits"
    [:p [:a {:href "/"} "Home"] " &gt; " [:a {:href "/kits/"} "Kit list"]]
    [:h1 "View commercial technologies"]
    (list [:p [:a {:href "/kits/analytes"} "By analyte detected"]]
          [:p [:a {:href "/kits/kitmanufacturers"} "By kit manufacturer"]]
          [:p [:a {:href "/kits/platforms"} "By platform used"]]
          [:p [:a {:href "/kits/kitlist"} "Or just show all kits"]])))

(defn home-page
  []
  (page-template
    "NO2"
    [:p [:a {:href "/"} "Home"]]
    [:h1 "QCMD NO2 Project"]
    (list [:p [:a {:href "/kits/"} "Explore commercial kits"]]
          [:p [:a {:href "/data/"} "10 Years of QCMD / Data Cleanup"]])))

(defn analyte-page
  []
  (page-template
    "Analyte list"
    [:p [:a {:href "/"} "Home"] " &gt; " [:a {:href "/kits/"} "Kit list"]]
    [:h1 "View kits by analyte detected"]
    (name-id-list (get-all-analytes) "analyte")))

(defn kit-manufacturer-page
  []
  (page-template
    "Kit manufacturer list"
    [:p [:a {:href "/"} "Home"] " &gt; " [:a {:href "/kits/"} "Kit list"]]
    [:h1 "View kits by manufacturer"]
    (name-id-list (get-all-kit-manufacturers) "manufacturer")))

(defn platform-page
  []
  (page-template
    "Platform list"
    [:p [:a {:href "/"} "Home"] " &gt; " [:a {:href "/kits/"} "Kit list"]]
    [:h1 "View kits by platform"]
    (name-id-list (get-all-platforms) "platform")))

(defn get-kit-query
  [params]
  (cond
    (= (get params :by) "analyte") (get-kits-by-analyte (get params :id))
    (= (get params :by) "platform") (get-kits-by-platform (get params :id))
    (= (get params :by) "manufacturer") (get-kits-by-manufacturer (get params :id))
    :else (get-all-kits)))

(defn kit-list-page
  [params]
  (page-template
    "Kit list"
    [:p [:a {:href "/"} "Home"] " &gt; " [:a {:href "/kits/"} "Kit list"]]
    [:h1 "Select kit"]
    (kit-list (get-kit-query params))))

(defn single-kit-page
  [kit-id]
  (let [kit-details (single-kit-name-tech kit-id)]
    (page-template
      (get kit-details :kitname)
      [:p [:a {:href "/"} "Home"] " &gt; " [:a {:href "/kits/"} "Kit list"]]
      [:h1 (get kit-details :kitname)]
      (list [:h2 "Was categorised as:"]
      [:p (get kit-details :techgroup)]
      [:h2 "Participants used it to test:"]
      [:p (single-kit-analytes kit-id)]
      [:h2 "And reported using the following platforms:"]
      [:p (single-kit-platforms kit-id)]))))