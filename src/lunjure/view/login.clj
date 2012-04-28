(ns lunjure.view.login
  (:use compojure.core
        ring.util.response
        lunjure_hiccster.content.home))

(defroutes login-routes
  (GET "/login" []
       (-> (page) (response))))
