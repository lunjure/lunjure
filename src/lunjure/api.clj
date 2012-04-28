(ns lunjure.api
  (:use lunjure.api.location
        compojure.core))

(def api-routes
  (routes (var location-routes)))
