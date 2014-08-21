(ns data-cleanup.connect)

(require '[clojure.java.jdbc :as j])

(def mysql-db {:subprotocol "mysql"
               :subname "//127.0.0.1:3306/technologies_lookup"
               :user "root"
               :password "xpass"})

(defn get-all-analytes
  []
  (j/query mysql-db
         ["select analyteID 'id', analyte 'name' from analytes ORDER BY name"]))

(defn get-analyte-from-id
  [analyte-id]
  (j/query mysql-db
           ["SELECT analyte FROM analytes WHERE analyteID = ?" analyte-id]
           :row-fn :analyte))

(defn get-refcodes
  [analyte-id]
  (j/query mysql-db
           ["SELECT t2.designID 'id', t2.RefCode 'name' FROM (SELECT programID FROM programround WHERE analyteID = ?)t1 INNER JOIN (SELECT programID, RefCode, designID FROM programmes)t2 ON t1.programID = t2.programID" analyte-id]))

(defn get-refcodes-with-pairs
  [analyte-id]
  (j/query mysql-db
           ["SELECT t2.designID 'id', t2.RefCode 'name', IFNULL(t3.lutes, '-') dilutions, IFNULL(t3.dupes, '-') duplicates FROM (SELECT programID FROM programround WHERE analyteID = ?)t1 INNER JOIN (SELECT programID, RefCode, designID FROM programmes)t2 ON t1.programID = t2.programID LEFT JOIN (SELECT i1.programID, GROUP_CONCAT(i1.duplicateconts) 'dupes', GROUP_CONCAT(i1.dilutionconts) 'lutes' FROM (SELECT programID, CASE WHEN pairtype = 'Duplicate' THEN samplecontent ELSE NULL END 'duplicateconts', CASE WHEN pairtype = 'Dilution' THEN samplecontent ELSE NULL END 'dilutionconts' FROM samplepairs ORDER BY samplecontent)i1 GROUP BY i1.programID)t3 ON t2.programID = t3.programID" analyte-id]
           :as-arrays? true))

(defn get-programmes
  [match]
  (j/query mysql-db
         ["SELECT programID, year, RefCode FROM programmes WHERE RefCode RLIKE ?" match]))

(defn get-panel-contents
  [design-id]
  (j/query mysql-db
           ["SELECT qc_panelrandomisation 'sampleCode', samplecontents, matrix, targetvalue, logvalues FROM qc_proposedpanelmembers WHERE designID = ?" design-id]
           :as-arrays? true))

(defn get-refcode-from-design
  [design-id]
  (j/query mysql-db
           ["SELECT RefCode FROM programmes WHERE designID = ?" design-id]
           :row-fn :refcode))

(defn get-programmes-from-regex
  [regex]
  (j/query mysql-db
           ["SELECT year, RefCode, programName FROM programmes WHERE RefCode RLIKE ?" regex]
           :as-arrays? true))

(defn commit-regex-to-db
  [regex analyte-id]
  (j/execute! mysql-db
            ["INSERT INTO programround (analyteID, roundID, distID, programID) SELECT ?, @a := @a + 1 'roundID', IF(t1.RefCode RLIKE 'B$', 2, 1) distID, t1.programID FROM programmes t1, (SELECT @a := 0) as a WHERE RefCode RLIKE ?" analyte-id regex]))

(defn get-last-panel
  [design-id]
  (j/query mysql-db
           ["SELECT t2.designID, t2.RefCode FROM (SELECT DISTINCT i2.analyteID, i2.roundID FROM qc_proposedpanelmembers i1 INNER JOIN programround i2 ON i1.programID = i2.programID WHERE i1.designID = ?)t1 INNER JOIN (SELECT i3.analyteID, i3.programID, i3.roundID, i4.RefCode, i4.designID FROM programround i3 INNER JOIN programmes i4 ON i3.programID = i4.programID)t2 ON t1.analyteID = t2.analyteID AND t1.roundID > t2.roundID ORDER BY designID DESC LIMIT 1" design-id]))

(defn get-next-panel
  [design-id]
  (j/query mysql-db
           ["SELECT t2.designID, t2.RefCode FROM (SELECT DISTINCT i2.analyteID, i2.roundID FROM qc_proposedpanelmembers i1 INNER JOIN programround i2 ON i1.programID = i2.programID WHERE i1.designID = ?)t1 INNER JOIN (SELECT i3.analyteID, i3.programID, i3.roundID, i4.RefCode, i4.designID FROM programround i3 INNER JOIN programmes i4 ON i3.programID = i4.programID)t2 ON t1.analyteID = t2.analyteID AND t1.roundID < t2.roundID ORDER BY designID ASC LIMIT 1" design-id]))

(defn get-sample-pairs
  [design-id]
  (j/query mysql-db
           ["SELECT t2.pairtype, t2.pairID, t2.samplecontent, t2.sampleCode1, t2.sampleCode2 FROM (SELECT DISTINCT programID FROM qc_proposedpanelmembers WHERE designID = ?) t1 INNER JOIN samplepairs t2 ON t1.programID = t2.programID ORDER BY t2.pairtype, t2.pairID" design-id]
           :as-arrays? true))

(defn get-sample-codes
  [design-id]
  (j/query mysql-db
           ["SELECT QC_PanelRandomisation AS 'sampleCode' FROM QC_ProposedPanelMembers WHERE designID = ? AND expectedqualitativeresult <> 'negative' ORDER BY QC_PanelRandomisation" design-id]
           :row-fn :samplecode))

(defn get-sample-contents
  [design-id]
  (j/query mysql-db
           ["SELECT DISTINCT sampleContents FROM QC_ProposedPanelMembers WHERE designID = ? AND expectedqualitativeresult <> 'negative' ORDER BY sampleContents" design-id]
           :row-fn :samplecontents))

(defn get-prepared-vector
  "Takes a prepared statement string, vector of keys and the http request parameters and constructs a prepared statement vector."
  [statement fields params]
  (into []
        (cons 
           statement
          ((fn [x] (map #(get x %) fields)) params))))

(defn save-sample-pair
  [params]
  (j/execute! mysql-db
              (get-prepared-vector
                "INSERT INTO samplepairs (programID, pairtype, pairID, samplecontent, sampleCode1, sampleCode2) SELECT t1.programID, ?, IFNULL(prID, 1), ?, ?, ? FROM (SELECT DISTINCT programID FROM QC_ProposedPanelMembers WHERE designID = ?)t1 LEFT JOIN (SELECT programID, pairtype, MAX(pairID) + 1 AS prID FROM samplepairs WHERE pairtype = ? GROUP BY programID, pairtype)t2 ON t1.programID = t2.programID"
                [:pairtype :paircontents :samplecode1 :samplecode2 :design-id :pairtype] params)))

(defn get-pids-for-pairs
  [analyte-id pair-type]
   (first (j/query mysql-db
                   ["SELECT GROUP_CONCAT(t1.programID) 'id' FROM (SELECT programID FROM programround WHERE analyteID = ?)t1 INNER JOIN (SELECT programID FROM samplepairs WHERE pairtype RLIKE ?)t2 ON t1.programID = t2.programID ORDER BY t1.programID" analyte-id pair-type]
                   :row-fn :id)))

(defn get-pair-results
  [analyte-id pair-type]
  (let [pids (get-pids-for-pairs analyte-id pair-type)]
    (j/query mysql-db
             [(str "SELECT t1.year, t1.pairID, t2.logres, t3.logres FROM (SELECT i3.year, i2.pairID, i2.sampleCode1, i2.sampleCode2 FROM programround i1 INNER JOIN samplepairs i2 ON i1.programID = i2.programID LEFT JOIN programmes i3 ON i2.programID = i3.programID WHERE i1.analyteID = ? AND i2.pairtype RLIKE ?)t1 "
                   "INNER JOIN (SELECT resultID, sampleCode, quant_log 'logres' FROM IndRes_Score WHERE QualitativeQuantitative RLIKE 'uant' AND program_ID IN (" pids "))t2 ON t1.sampleCode1 = t2.sampleCode INNER JOIN "
                   "(SELECT resultID, sampleCode, quant_log 'logres' FROM IndRes_Score WHERE QualitativeQuantitative RLIKE 'uant' AND program_ID IN (" pids "))t3 ON t1.sampleCode2 = t3.sampleCode AND t2.resultID = t3.resultID") analyte-id pair-type]
             :as-arrays? true)))

(defn get-pair-query
  [analyte-id pair-type]
  (let [pids (get-pids-for-pairs analyte-id pair-type)]
    (str "SELECT t1.year, t1.pairID, t2.logres, t3.logres FROM (SELECT i3.year, i2.pairID, i2.sampleCode1, i2.sampleCode2 FROM programround i1 INNER JOIN samplepairs i2 ON i1.programID = i2.programID LEFT JOIN programmes i3 ON i2.programID = i3.programID WHERE i1.analyteID = ? AND i2.pairtype RLIKE ?)t1 "
                   "INNER JOIN (SELECT resultID, sampleCode, LOG10(QuantitativeResult) 'logres' FROM QC_ProgramResultsData WHERE programID IN (" pids "))t2 ON t1.sampleCode1 = t2.sampleCode INNER JOIN "
                   "(SELECT resultID, sampleCode, LOG10(QuantitativeResult) 'logres' FROM QC_ProgramResultsData WHERE programID IN (" pids "))t3 ON t1.sampleCode2 = t3.sampleCode AND t2.resultID = t3.resultID")))