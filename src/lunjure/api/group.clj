(ns lunjure.api.group
  (:use lunjure.util
        compojure.core
        lamina.core
        aleph.http)
  (:require [lunjure.db :as db]))

(defonce group-channels
  (atom {}))

(defn get-group-channel [group]
  (swap! group-channels
         update-in
         [group]
         (fn [ch]
           (or ch (permanent-channel))))
  (get @group-channels group))

(defn enrich-message [user msg]
  (-> (read-string msg)
      (assoc :user user
             :time (now))))

(defn user-group-chat-handler [group-channel group-id user]
  (fn [user-channel handshake]
    (siphon group-channel user-channel)
    (let [user-channel (map* (partial enrich-message user) user-channel)]
      (-> (map* pr-str user-channel)
          (siphon group-channel))
      (-> (fork user-channel)
          (receive-in-order (partial db/add-message! group-id))))))

(defroutes group-routes
  (GET "/groups/:id/socket" [id :as req]
       (-> (get-group-channel id)
           (user-group-chat-handler id (-> req :session :user))
           (wrap-aleph-handler))))
