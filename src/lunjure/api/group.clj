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

(def time-string-formatter
  (java.text.SimpleDateFormat. "HH:mm"))

(defn enrich-message
  ([user msg]
     (enrich-message (-> (read-string msg)
                         (assoc :user user))))
  ([msg]
     (let [time (now)]
       (-> (if (map? msg) msg )
           (assoc :time time
                  :time-string
                  (->> (java.util.Date. time)
                       (.format time-string-formatter)))
           (pr-str)))))

(defn send-message [channel message]
  (enqueue channel (enrich-message message)))

(defn user-group-chat-handler [group-channel group-id user]
  (fn [user-channel handshake]
    (siphon group-channel user-channel)
    (send-message group-channel {:type :enter :user user})
    (let [user-channel (map* (partial enrich-message user) user-channel)]
      (siphon user-channel group-channel)
      (-> (fork user-channel)
          (receive-in-order (partial db/add-message! group-id))))))

(defroutes group-routes
  (GET "/groups/:id/socket" [id :as req]
       (-> (get-group-channel id)
           (user-group-chat-handler id (-> req :session :user))
           (wrap-aleph-handler))))
