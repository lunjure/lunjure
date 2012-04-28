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
                    (.format time-string-formatter))))))

(defn broadcast-message [group-channel group-id user message]
  (let [message (enrich-message user message)]
    (enqueue group-channel (pr-str message))
    (db/add-message! group-id message)))

(defn send-history [channel group-id]
  (let [threshold (- (now) (* 1000 (hours 12)))]
    (doseq [message (->> (db/get-messages group-id 30)
                         (filter (fn [m] (and (:time m) (> (:time m) threshold))))
                         (reverse)
                         (map pr-str))]
      (enqueue channel message))))

(defn user-group-chat-handler [group-channel group-id user]
  (fn [user-channel handshake]
    (siphon group-channel user-channel)
    (on-closed user-channel (partial broadcast-message group-channel group-id user {:type :exit}))
    (send-history user-channel group-id)
    (broadcast-message group-channel group-id user {:type :enter})
    (let [user-channel (map* (partial enrich-message user) user-channel)]
      (-> (fork user-channel)
          (receive-in-order (partial db/add-message! group-id)))
      (siphon (map* pr-str user-channel) group-channel))))

(defroutes group-routes
  (GET "/groups/:id/socket" [id :as req]
       (-> (get-group-channel id)
           (user-group-chat-handler id (-> req :session :user))
           (wrap-aleph-handler))))
