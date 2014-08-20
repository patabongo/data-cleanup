(ns data-cleanup.csv
  (:require [data-cleanup.connect :refer :all]
        [clojure.data.csv :as csv]
        [noir.response :refer [content-type status]]))

(defn nameify-keys
  [result-set]
  (cons 
    (vec (map #(name %) (first result-set))) 
    (rest result-set)))

#_(comment 
    "These functions were for CSV output based on an exercise in the Bulletproof textbook. Too complicated!"
(defn write-response
  [csv-bytes]
  (with-open [in (java.io.ByteArrayInputStream. csv-bytes)]
    (-> (response/response in)
      (response/header "Content-Disposition" "filename=file.csv")
      (response/content-type "text/csv"))))

(defn output-csv
  [analyte-id pair-type]
  (let [out (new java.io.ByteArrayOutputStream)]
    (with-open [my-csv (io/writer out)]
      (csv/write-csv my-csv (nameify-keys (get-pair-results analyte-id pair-type))))
    (write-response (.toByteArray out)))))

(defn csv
  "Formats content as csv and prompts the user to save"
  [analyte-id pair-type]
  (assoc-in
    (content-type "text/csv"
                  (str (doto (java.io.StringWriter.) (csv/write-csv (nameify-keys (get-pair-results analyte-id pair-type))))))
    [:headers "Content-Disposition"]
    (str "attachment;filename=" (first (get-analyte-from-id analyte-id)) "_" pair-type ".csv")))