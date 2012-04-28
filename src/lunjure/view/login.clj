(ns lunjure.view.login
  (:use compojure.core
        ring.util.response
        lunjure.http
        lunjure_hiccster.content.home
        lunjure.demo))

(defroutes login-routes
  (GET "/login" req
       (if-let [user (-> req :params :user)]
         (-> (redirect (str "/groups/" *default-group-id*))
             (assoc-in [:session :user] user))
         (html-response (page)))))
