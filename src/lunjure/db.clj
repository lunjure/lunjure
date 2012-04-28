(ns
    ^{:doc "This namespace encapsulates all functions for a"}
  lunjure.db
  (:require [clj-redis.client :as redis]
            [lunjure.util :as util]))

(def db (redis/init))

(def ^{:private true} GROUPS_NS "lunjure.groups")
(def ^{:private true} GROUPS_KEY "lunjure/groups")
(def ^{:private true} USERS_NS "lunjure.users")
(def ^{:private true} USERS_KEY "lunjure/users")

(defn- group-key [name] (str GROUPS_NS "/" name))
(defn- group-messages-key [id] (str GROUPS_NS ".messages/" id))
(defn- group-invations-key [id] (str GROUPS_NS ".invitations/" id))
(defn- group-members-key [id] (str GROUPS_NS ".members/" id))
(defn- group-location-key [id] (str GROUPS_NS ".location/" id))
(defn- group-venues-key [id] (str GROUPS_NS ".venues/" id))

(defn- user-key [id] (str USERS_NS "/" id))

(defn create-user!
  "Creates the user with the given name. Dummy until we have
   implemented authentication via Foursquare."
  [user-name]
  (if-let [added? (= 1 (redis/sadd db USERS_KEY user-name))]
    (let [id (util/new-uuid)]
      (redis/set db (user-key id) user-name)
      {:id id :name user-name})
    {:error (str "User with name " user-name " already exists.")}))

(defn create-group!
  "Creates a group with the given name."
  [group-name]
  (if-let [added? (= 1 (redis/sadd db GROUPS_KEY group-name))]
    (let [id (util/new-uuid)]
      (redis/set db (group-key group-name) id)
      {:id id :name group-name})
    {:error (str "Group with name " group-name " already exists.")}))

(defn add-invitation!
  "Adds an invitation for the specified  user to the given group."
  [group-id user-id]
  (= 1 (redis/sadd db (group-invations-key group-id) user-id)))

(defn add-group-member!
  "Adds the specified user to the given group."
  [group-id user-id]
  (= 1 (redis/sadd db (group-members-key group-id) user-id)))

(defn add-message!
  "Adds the specified message to the end of the message list of the given group."
  [group-id message]
  (redis/lpush db (group-messages-key group-id) (pr-str message)))

(defn get-messages
  "Returns the messages for the given group."
  ([group-id start end]
     (map read-string (redis/lrange db (group-messages-key group-id) start end)))
  ([group-id number-of-messages]
     (get-messages group-id 0 (- number-of-messages 1)))
  ([group-id]
     (get-messages group-id 0 Integer/MAX_VALUE)))

(defn get-group-members
  "Returns all members of the given group."
  [group-id]
  (set (redis/smembers db (group-members-key group-id))))

(defn get-invited-users
  "Returns all invited users of the given group."
  [group-id]
  (set (redis/smembers db (group-invations-key group-id))))

(defn get-group-location
  "Returns the current geo-location of the specified group."
  [group-id]
  (when-let [location (redis/get db (group-location-key group-id))]
    (read-string location)))

(defn set-group-location!
  "Sets the current geo-location of the specified group."
  [group-id location]
  (redis/set db (group-location-key group-id) (pr-str location)))

(defn get-venues-for-group
  "Returns all venues cached for the specified group."
  [group-id]
  (when-let [venues (redis/get db (group-venues-key group-id))]
    (read-string venues)))

(defn set-venues-for-group
  "Updates the cache of venues for the specified group."
  [group-id venues]
  (redis/setex db (group-venues-key group-id) (util/days 1) (pr-str venues)))

(defn get-group-id-by-name [name]
  (redis/get db (group-key name)))
