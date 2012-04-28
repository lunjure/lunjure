(ns lunjure.view
  (:use compojure.core
        ring.util.response
        lunjure.view.login))

(defroutes view-routes
  login-routes
  (constantly (not-found "Nil.")))
