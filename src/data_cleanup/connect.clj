(ns data-cleanup.connect)

(require '[clojure.java.jdbc :as j])

(def mysql-db {:subprotocol "mysql"
               :subname "//127.0.0.1:2470/technologies_lookup"
               :user "root"
               :password "G74vCSse"})

(defn get-all-analytes
  []
  (j/query mysql-db
         ["select analyteID 'id', analyte 'name' from analytes ORDER BY name"]))

(defn get-refcodes
  [analyte-id]
  (j/query mysql-db
           ["SELECT t2.designID 'id', t2.RefCode 'name' FROM (SELECT programID FROM programround WHERE analyteID = ?)t1 INNER JOIN (SELECT programID, RefCode, designID FROM programmes)t2 ON t1.programID = t2.programID" analyte-id]))

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