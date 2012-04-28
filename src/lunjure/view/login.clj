(ns lunjure.view.login
  (:use compojure.core
        lunjure.http
        lunjure_hiccster.content.home))

(defroutes login-routes
  (GET "/login" []
       (html-response (page))))
