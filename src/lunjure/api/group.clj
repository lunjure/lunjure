(ns lunjure.api.group
  (:use compojure.core
        lamina.core
        aleph.http))

(defonce group-channels
  (atom {}))

(defn get-group-channel [group]
  (swap! group-channels
         update-in
         [group]
         (fn [ch]
           (or ch (permanent-channel))))
  (get @group-channels group))

(defn add-user [msg user]
  (pr-str (assoc (read-string msg) :user user)))

(defn user-group-chat-handler [group-channel user]
  (fn [user-channel handshake]
    (siphon group-channel user-channel)
    (-> (map* (fn [m]
                (add-user m user))
              user-channel)
        (siphon group-channel))))

(defroutes group-routes
  (GET "/groups/:id/socket" [id :as req]
       (-> (get-group-channel id)
           (user-group-chat-handler (-> req :session :user))
           (wrap-aleph-handler))))
