(ns data-cleanup.handler
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [data-cleanup.view :refer :all]))

(defroutes app-routes
  (GET "/" request (analyte-page request))
  (GET "/foo/" request (foopage request))
  (GET "/foo/bar/" request (barpage request))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
  
