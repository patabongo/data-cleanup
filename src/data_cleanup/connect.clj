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

(defn get-programmes
  [match]
  (j/query mysql-db
         ["SELECT programID, year, RefCode FROM programmes WHERE RefCode RLIKE ?" match]))