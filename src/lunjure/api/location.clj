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

(defn display-name [location]
  (apply str (interpose ", " (map location [:name :address :city]))))

(defroutes location-routes
  (GET "/groups/:group-id/locations" [group-id :as req]
       (-> (if-let [term (-> req :params :term)]
             (map display-name (locations/find-locations-by-prefix group-id term))
             [])
           (sort)
           (json-response))))
