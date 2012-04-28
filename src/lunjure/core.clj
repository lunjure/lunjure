(ns lunjure.core
  (:use aleph.http
        ring.util.response
        ring.middleware.file
        ring.middleware.file-info
        ring.middleware.session
        ring.middleware.params
        ring.middleware.keyword-params
        compojure.core
        lunjure.view
        lunjure.api)
  (:require [swank.swank :as swank]))

(defn wrap-auth [handler]
  (fn [req]
    (let [user (-> req :session :user)]
      (if (or user (= "/login" (-> req :uri)))
        (handler (assoc req :user user))
        (redirect "/login")))))<

(def app
  (-> (routes
       (var api-routes)
       (var view-routes))
      (wrap-auth)
      (wrap-session)
      (wrap-keyword-params)
      (wrap-params)
      (wrap-file "resources/public")
      (wrap-file "design/static")
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
