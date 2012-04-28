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

(defn read-cljs-string [str]
  (-> (.replaceAll str "\\\\x" "\\\\u00")
      (read-string)))

(def time-string-formatter
  (java.text.SimpleDateFormat. "HH:mm"))

(defn enrich-message [user msg]
  (let [time (now)]
    (-> (if (map? msg) msg (read-cljs-string msg))
        (assoc :user user
               :time time
               :time-string
               (->> (java.util.Date. time)
                    (.format time-string-formatter)))
        (pr-str))))

(defn send-message [channel user message]
  (enqueue channel (enrich-message user message)))

(defn user-group-chat-handler [group-channel group-id user]
  (fn [user-channel handshake]
    (siphon group-channel user-channel)
    (on-closed user-channel (partial send-message group-channel user {:type :exit}))
    (send-message group-channel user {:type :enter})
    (let [user-channel (map* (partial enrich-message user) user-channel)]
      (siphon user-channel group-channel)
      (-> (fork user-channel)
          (receive-in-order (partial db/add-message! group-id))))))

(defroutes group-routes
  (GET "/groups/:id/socket" [id :as req]
       (-> (get-group-channel id)
           (user-group-chat-handler id (-> req :session :user))
           (wrap-aleph-handler))))
