(ns lunjure.api.location
  (:use lunjure.http
        compojure.core)
  (:require [lunjure.locations :as locations]))

(def dummy-locations
  #{"REWE" "Russland" "Rumpelkammer" "REAL" "Rakete"})

(defn find-locations-like [s]
  (let [s (.toLowerCase s)]
    (filter (fn [x]
              (-> (.toLowerCase x)
                  (.startsWith s)))
            dummy-locations)))

(defroutes location-routes
  (GET "/locations" req
       (-> (if-let [term (-> req :params :term)]
             (map :name (locations/find-locations-by-prefix token))
             [])
           (sort)
           (json-response))))
