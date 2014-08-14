(ns commercial-kits.connect)

(require '[clojure.java.jdbc :as j])

(def mysql-db {:subprotocol "mysql"
               :subname "//127.0.0.1:3306/technologies_lookup"
               :user "root"
               :password "xpass"})

(defn get-all-analytes
  []
  (j/query mysql-db
         ["select analyteID 'id', analyte 'name' from analytes ORDER BY name"]))

(defn get-all-kit-manufacturers
  []
  (j/query mysql-db
           ["SELECT manID 'id', manufacturer 'name' FROM kitmanufacturers ORDER BY name"]))

(defn get-all-kits
  []
  (j/query mysql-db
           ["SELECT t2.kitID 'id', CONCAT(t1.manufacturer, ' ', t2.kit) 'name' FROM kitmanufacturers t1 INNER JOIN commercialkits t2 ON t1.manID = t2.manID ORDER BY name"]))

(defn get-all-platforms
  []
  (j/query mysql-db
           ["SELECT t2.platformID 'id', CONCAT(t1.platformman, ' ', t2.platform) 'name' FROM platformmanufacturers t1 INNER JOIN platforms t2 ON t1.platmanID = t2.platmanID ORDER BY name"]))

(defn get-kits-by-analyte
  [analyte-id]
  (j/query mysql-db
           ["SELECT t2.kitID 'id', CONCAT(t1.manufacturer, ' ', t2.kit) 'name' FROM kitmanufacturers t1 INNER JOIN commercialkits t2 ON t1.manID = t2.manID INNER JOIN kitanalyte t3 ON t2.kitID = t3.kitID WHERE t3.analyteID = ? ORDER BY name" analyte-id]))

(defn get-kits-by-platform
  [platform-id]
  (j/query mysql-db
           ["SELECT t2.kitID 'id', CONCAT(t1.manufacturer, ' ', t2.kit) 'name' FROM kitmanufacturers t1 INNER JOIN commercialkits t2 ON t1.manID = t2.manID INNER JOIN platformskits t3 ON t2.kitID = t3.kitID WHERE t3.platformID = ? ORDER BY name" platform-id]))

(defn get-kits-by-manufacturer
  [manufacturer-id]
  (j/query mysql-db
           ["SELECT t2.kitID 'id', CONCAT(t1.manufacturer, ' ', t2.kit) 'name' FROM kitmanufacturers t1 INNER JOIN commercialkits t2 ON t1.manID = t2.manID WHERE t1.manID = ? ORDER BY name" manufacturer-id]))

(defn single-kit-name-tech
  [kit-id]
  (first (j/query mysql-db
                  ["SELECT CONCAT(t1.manufacturer, ' ', t2.kit) 'kitname', techGroup FROM kitmanufacturers t1 INNER JOIN commercialkits t2 ON t1.manID = t2.manID WHERE t2.kitID = ?" kit-id])))

(defn single-kit-analytes
  [kit-id]
  (j/query mysql-db
         ["SELECT GROUP_CONCAT(analyte SEPARATOR ', ') 'kitanalytes' FROM analytes t1 INNER JOIN kitanalyte t2 ON t1.analyteID = t2.analyteID WHERE t2.kitID = ?" kit-id]
         :row-fn :kitanalytes))

(defn single-kit-platforms
  [kit-id]
  (j/query mysql-db
         ["SELECT GROUP_CONCAT(CONCAT(t3.platformman, ' ', t2.platform) SEPARATOR ', ') 'kitplatforms' FROM platformskits t1 INNER JOIN platforms t2 ON t1.platformID = t2.platformID INNER JOIN platformmanufacturers t3 ON t2.platmanID = t3.platmanID WHERE t1.kitID = ?" kit-id]
         :row-fn :kitplatforms))