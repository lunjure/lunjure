(ns lunjure.api
  (:use [lunjure.api location group]
        compojure.core))

(def api-routes
  (routes
   (var location-routes)
   (var group-routes)))
