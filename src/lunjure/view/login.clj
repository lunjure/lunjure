(ns lunjure.view.login
  (:use compojure.core
        clojure.set
        ring.util.response
        lunjure.http
        lunjure_hiccster.content.home
        lunjure.demo)
  (:require [clj-foursquare.auth :as auth]
            [clj-foursquare.users :as users]
            [lunjure.db :as db]
            [lunjure.foursquare :as foursquare]))

(defroutes login-routes

  (GET "/login" req
       (if-let [user (-> req :params :user)]
         (-> (redirect (str "/groups/" *default-group-id*))
             (assoc-in [:session :user] user))
         (html-response (page))))

  (POST "/login" req
        (let [authz-request (auth/authz-request foursquare/client)]
          (redirect (:uri authz-request))))

  (GET "/oauth2/authorize" req
       (if-let [code (get-in req [:params :code])]
         (let [token
               (try (auth/get-access-token foursquare/client (:params req))
                    (catch Exception e (redirect "/login")))
               foursquare-user (users/self token)]
           (db/store-foursquare-user! foursquare-user)
           (-> (redirect "/")
               (assoc-in [:session :user] (assoc (rename-keys (select-keys foursquare-user [:id :firstName :lastName])
                                                              {:firstName :first-name :lastName :last-name})
                                            :access-token token))))
          (redirect "/login"))))
