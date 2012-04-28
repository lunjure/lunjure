(ns lunjure.api
  (:use lunjure.api.location
        compojure.core
        ring.middleware.params
        ring.middleware.keyword-params))

(def api-routes
  (-> (var location-routes)
      (wrap-keyword-params)
      (wrap-params)))
