(ns lunjure.core
  (:use aleph.http
        ring.util.response
        ring.middleware.file
        ring.middleware.file-info
        compojure.core
        lunjure.view
        lunjure.api)
  (:require [swank.swank :as swank]))

(def app
  (-> (routes
       (var api-routes)
       (var view-routes))
      (wrap-file "design/static")
      (wrap-file "resources/public")
      (wrap-file-info)))

(defn start-aleph [& [{:keys [port] :or {port 8080}}]]
  (let [stop-aleph (-> (wrap-ring-handler (var app))
                       (start-http-server {:port port
                                           :websocket true}))]
    (intern (find-ns 'lunjure.core) 'stop-aleph stop-aleph)
    (println "Lunjure listening on port" port)))

(defn -main []
  (start-aleph)
  (swank/start-server :host "localhost" :port 4005))
