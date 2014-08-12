(ns data-cleanup.handler
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [data-cleanup.view :refer :all]))

(defroutes app-routes
  (GET "/" [] (analyte-page))
  (GET "/analyte/:id" [id] (refcodes-switch id)) 
  (GET "/design/:id" [id] (panel-contents-page id))
  (POST "/regex/" [regex analyte-id] (check-regex-page regex analyte-id))
  (POST "/submit/" [regex analyte-id] (submit-regex regex analyte-id))
  (POST "/pairsave/" {params :params} (pair-save params))
  (POST "/foo/" {params :params} (foopage params))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
  
