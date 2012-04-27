(ns lunjure.core
  (:use aleph.http
        ring.util.response)
  (:require [swank.swank :as swank]))

(defn app [request]
  (not-found "Nil."))

(defn start-aleph [& [{:keys [port] :or {port 8080}}]]
  (let [stop-aleph (start-http-server (wrap-ring-handler (var app)) {:port port})]
    (intern (find-ns 'lunjure.core) 'stop-aleph stop-aleph)
    (println "Lunjure listening on port" port)))

(defn -main []
  (start-aleph)
  (swank/start-server :host "localhost" :port 4005))