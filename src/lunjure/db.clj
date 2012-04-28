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

(defn- group-key [id] (str GROUPS_NS "/" id))
(defn- group-messages-key [id] (str GROUPS_NS ".messages/" id))
(defn- group-invations-key [id] (str GROUPS_NS ".invitations/" id))
(defn- group-members-key [id] (str GROUPS_NS ".members/" id))
(defn- group-location-key [id] (str GROUPS_NS ".location/" id))
(defn- group-venues-key [id] (str GROUPS_NS ".venues/" id))
(defn- team-key [group-id team-id] (str GROUPS_NS "." group-id ".teams/" team-id))
(defn- teams-key [group-id] (str GROUPS_NS ".teams/" group-id))

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
      (redis/set db (group-key id) group-name)
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

(defn create-team!
  "Creates a new team that belongs to the specified group."
  [group-id team]
  (let [team-id (util/new-uuid)]
    (redis/sadd db (teams-key group-id) team-id)
    (redis/set db (team-key group-id team-id) (pr-str team))
    (assoc team :id team-id)))

(defn get-teams
  "Returns all teams of the given group."
  [group-id]
  (let [team-ids (redis/smembers db (teams-key group-id))]
    (map read-string (apply redis/mget db (map (partial team-key group-id) team-ids)))))

(defn add-to-team!
  "Adds the user with the specified id to the given team."
  [team-id user-id])

(defn remove-from-team!
  "Removes the specified user from the team with the given id."
  [team-id user-id]
  )

(defn get-team-members
  "Returns a list of all user ids of all members of the given team."
  [team-id])