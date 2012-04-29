(ns lunjure.demo
  (:require [lunjure.db :as db])
  (:use compojure.core
        ring.util.response))

(def ^{:dynamic true} *default-group-id*)

(defroutes demo-routes

  (GET "/" req
       (redirect (str "/groups/" *default-group-id*))))

(defn init []
  (db/create-group! "ah2012")
  (alter-var-root (var *default-group-id*)
                  (constantly (db/get-group-id-by-name "ah2012")))
 
  ;; (db/set-group-location! *default-group-id* {:latitude 50.948731 :longitude 6.986625})
  (db/set-group-location! *default-group-id* {:latitude 50.733992 :longitude 7.099814}))
