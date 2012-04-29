(ns lunjure.api.group
  (:use lunjure.util
        compojure.core
        lamina.core
        aleph.http)
  (:require [lunjure.db :as db])
  (:import [org.joda.time
            DateTime
            DateMidnight
            DateTimeFieldType]))

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

(defn format-time
  ([millis]
     (->> (java.util.Date. millis)
          (.format time-string-formatter)))
  ([hours minutes]
     (-> (.. (DateTime.)
             (withField (DateTimeFieldType/hourOfDay)
                        (Integer/parseInt hours))
             (withField (DateTimeFieldType/minuteOfHour)
                        (Integer/parseInt minutes))
             (getMillis))
         (format-time))))

(defn parse-time [time]
  (condp re-find time
    #"^(\d{1,2})(\d{1,2})$" :>>
    (fn [[_ h m]] (format-time h m))
    #"^(\d{1,2}):(\d{1,2})$" :>>
    (fn [[_ h m]] (format-time h m))))

(defn enrich-message [user msg]
  (let [time (now)]
    (-> (if (map? msg) msg (read-cljs-string msg))
        (assoc :user user
               :time time
               :time-string
               (format-time time)))))

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

(defmulti handle-message :type)

(defmethod handle-message :default [message]
  message)

(defmethod handle-message :team [{:keys [name lunch-time location] :as message}]
  (let [time (parse-time lunch-time)]
    (assoc message
      :lunch-time time)))

(defn user-group-chat-handler [group-channel group-id user]
  (fn [user-channel handshake]
    (siphon group-channel user-channel)
    (on-closed user-channel (partial broadcast-message group-channel group-id user {:type :exit}))
    (send-history user-channel group-id)
    (broadcast-message group-channel group-id user {:type :enter})
    (let [user-channel (map* (partial enrich-message user) user-channel)]
      (-> (fork user-channel)
          (receive-in-order (partial db/add-message! group-id)))
      (receive-all user-channel (bound-fn* prn))
      (let [ch (map* handle-message user-channel)]
        (receive-all ch (bound-fn* prn))
        (siphon (map* pr-str ch) group-channel)))))

(defn user->string [user]
  (str (:first-name user) " " (:last-name user)))

(defroutes group-routes
  (GET "/groups/:id/socket" [id :as req]
       (-> (get-group-channel id)
           (user-group-chat-handler id (-> req :session :user))
           (wrap-aleph-handler))))
