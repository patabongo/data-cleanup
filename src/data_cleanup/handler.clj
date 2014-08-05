(ns data-cleanup.handler
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [data-cleanup.view :refer :all]))

(defroutes app-routes
  (GET "/" [] (analyte-page))
  (GET "/analyte/:id" [id] (refcodes-page id)) 
  (GET "/foo/" request (foopage request))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
  
