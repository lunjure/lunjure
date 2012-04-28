(ns lunjure.locations
  (:require [clj-foursquare.venues :as venues]
            [lunjure.foursquare :as foursquare]
            [lunjure.db :as db]))

(def category-filter "4d4b7105d754a06374d81259,4bf58dd8d48988d1f9941735,4d954b0ea243a5684a65b473")

(defn- get-fresh-locations
  "Returns locations for the specified group, directly querying the Foursquare API
   without hitting the cache."
  [group-id]
  (let [{:keys [latitude longitude]} (db/get-group-location group-id)]
    (venues/search foursquare/client latitude longitude :radius 2000 :category-id category-filter)))

(defn- get-locations
  "Returns all locations for the specified group, updating the cache if necesssary."
  [group-id]
  (if-let [locations (db/get-venues-for-group group-id)]
    locations
    (let [locations (get-fresh-locations group-id)]
      (db/set-venues-for-group group-id locations)
      locations)))

(defn- filter-locations-by-prefix
  "Filters the given locations by the specified prefix."
  [locations prefix]
  (filter #(.startsWith (.toLowerCase (:name %)) (.toLowerCase prefix)) locations))

(defn find-locations-by-prefix
  "Finds all locations for the given group whose name starts with the given prefix."
  [group-id prefix]
  (let [locations (get-locations group-id)]
    (filter-locations-by-prefix locations prefix)))
