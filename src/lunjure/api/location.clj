(ns lunjure.api.location
  (:use lunjure.http
        compojure.core))

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
       (-> (if-let [token (-> req :params :token)]
             (find-locations-like token)
             dummy-locations)
           (sort)
           (json-response))))
