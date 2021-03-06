(ns data-cleanup.handler
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [data-cleanup.view :refer :all]
            [data-cleanup.csv :refer :all]
            [commercial-kits.view :as view]))

(defroutes app-routes
  (GET "/" [] (view/home-page))
  (route/resources "/")
  (route/not-found "Not Found"))

(defroutes data-routes
 (context "/data" []
          (GET "/" [] (analyte-page))
          (GET "/analyte/:id" [id] (refcodes-switch id))
          (GET "/design/:id" [id] (panel-contents-page id))
          (POST "/regex/" [regex analyte-id] (check-regex-page regex analyte-id))
          (POST "/submit/" [regex analyte-id] (submit-regex regex analyte-id))
          (POST "/pairsave/" {params :params} (pair-save params))
          (GET "/catnegs/:id" [id] (catnegs id))
          (POST "/negatives/" {params :params} (negatives params))))

(defroutes kit-routes
  (context "/kits" []
           (GET "/" [] (view/index-page))
           (GET "/analytes" [] (view/analyte-page))
           (GET "/kitmanufacturers" [] (view/kit-manufacturer-page))
           (GET "/platforms" [] (view/platform-page))
           (GET "/kitlist" {params :params} (view/kit-list-page params))
           (GET "/kit/:id" [id] (view/single-kit-page id))))

(defroutes csv-routes
  (context "/csv/:id" [id]
           (GET "/duplicate" [] (csv id "duplicate"))
           (GET "/dilution" [] (csv id "dilution"))))

(def app
  (handler/site (routes csv-routes data-routes kit-routes app-routes)))