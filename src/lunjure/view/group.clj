(ns lunjure.view.group
  (:use compojure.core
        lunjure.http
        lunjure_hiccster.content.lunjure))

(defroutes group-routes
  (GET "/group/:id" [id]
       (html-response (page false))))
