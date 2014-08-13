(ns utility-cleanup.db-cleanup-functions)

(require '[clojure.java.jdbc :as j])

(def mysql-db {:subprotocol "mysql"
               :subname "//127.0.0.1:3306/technologies_lookup"
               :user "root"
               :password "xpass"})

(defn get-result-ids
  "Originally had this without the let/if, but the reduce errors if there are no query results."
  [program-id]
  (let [x (j/query mysql-db
                        ["SELECT resultID FROM QC_ProgramResults WHERE programID = ?" program-id]
                        :row-fn :resultid)]
    (if (empty? x)
      "(0)"
      (str "(" (reduce #(str % ", " %2) x) ")"))))

(defn update-program-ids
  "Used this to add programIDs to the results data table by mapping it on a range of programIDs."
  [program-id]
  (j/execute! mysql-db
              [(str "UPDATE QC_ProgramResultsData SET programID = ? WHERE resultID IN " (get-result-ids program-id)) program-id ]))

