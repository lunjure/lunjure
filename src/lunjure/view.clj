(ns lunjure.view
  (:use compojure.core
        ring.util.response
        [lunjure.view login group]))

(defroutes view-routes
  (var login-routes)
  (var group-routes)
  (constantly (not-found "Nil.")))
