(ns data-cleanup.csv
  (:require [data-cleanup.connect :refer :all]
        [clojure.data.csv :as csv]
        [noir.response :refer [content-type status]]))

(defn nameify-keys
  [result-set]
  (cons 
    (vec (map #(name %) (first result-set))) 
    (rest result-set)))

(defn csv
  "Formats content as csv and prompts the user to save"
  [analyte-id pair-type]
  (assoc-in
    (content-type "text/csv"
                  (str (doto (java.io.StringWriter.) (csv/write-csv (nameify-keys (get-pair-results analyte-id pair-type))))))
    [:headers "Content-Disposition"]
    (str "attachment;filename=" (first (get-analyte-from-id analyte-id)) "_" pair-type ".csv")))