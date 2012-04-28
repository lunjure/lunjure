(ns lunjure.view.login
  (:use compojure.core
        ring.util.response
        lunjure.http
        lunjure_hiccster.content.home))

(defroutes login-routes
  (GET "/login" req
       (if-let [user (-> req :params :user)]
         (-> (redirect "/")
             (assoc-in [:session :user] user))
         (html-response (page)))))
