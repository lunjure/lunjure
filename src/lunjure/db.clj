(ns
    ^{:doc "This namespace encapsulates all functions for a"}
  lunjure.db
  (:require [clj-redis.client :as redis]
            [lunjure.util :as util]))

(def db (redis/init))

(def ^{:private true} TEAMS_NS "lunjure.teams")
(def ^{:private true} GROUPS_NS "lunjure.groups")
(def ^{:private true} GROUPS_KEY "lunjure/groups")
(def ^{:private true} USERS_NS "lunjure.users")
(def ^{:private true} USERS_KEY "lunjure/users")

(defn- group-key [name] (str GROUPS_NS "/" name))
(defn- group-messages-key [id] (str GROUPS_NS "." id "/messages"))
(defn- group-invations-key [id] (str GROUPS_NS "." id "/invitations"))
(defn- group-members-key [id] (str GROUPS_NS "." id "/members"))
(defn- group-location-key [id] (str GROUPS_NS "." id "/location"))
(defn- group-venues-key [id] (str GROUPS_NS "." id "/venues"))
(defn- team-key [group-id team-name] (str GROUPS_NS "." group-id ".teams/" team-name))
(defn- teams-key [group-id] (str GROUPS_NS "." group-id "/teams"))
(defn- team-members-key [group-id team-name] (str GROUPS_NS  "." group-id ".teams." team-name "/members"))
(defn- user-key [id] (str USERS_NS "/" id))
(defn- team-membership-key [group-id user-id] (str GROUPS_NS "." group-id ".user." user-id "/team"))

(defn store-foursquare-user!
  "Stores the specified Foursquare user so that it can be retrieved
   by its Foursquare user id."
  [user]
  (if-let [added? (= 1 (redis/sadd db USERS_KEY (:id user)))]
    (do
      (redis/set db (user-key (:id user)) (pr-str user))
      user)
    {:error (str "User with id " (:id user) " already exists.")}))

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
  (redis/set db (group-location-key group-id) (pr-str location))
  (redis/del db [(group-venues-key group-id)]))

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
  (let [team-name (:name team)]
    (if-let [added? (= 1 (redis/sadd db (teams-key group-id) team-name))]
      (do (redis/set db (team-key group-id team-name) (pr-str team))
          team)
      {:error (str "Team with name " team-name " already exists.")})))

(defn get-teams
  "Returns all teams of the given group."
  [group-id]
  (let [team-names (redis/smembers db (teams-key group-id))]
    (->> team-names
         (map (partial team-key group-id))
         (apply redis/mget db)
         (map read-string))))

(defn team-exists?
  "Does the given team exist?"
  [group-id team-name]
  (redis/exists db (team-key group-id team-name)))

(defn add-to-team!
  "Adds the user with the specified id to the given team."
  [group-id team-name user-id]
  (let [mkey (team-membership-key group-id user-id)
        prev-team (redis/get db mkey)]
    (when (not (nil? prev-team))
      (redis/srem db (team-members-key group-id prev-team) user-id))
    (redis/sadd db (team-members-key group-id team-name) user-id)
    (redis/set db (team-membership-key group-id user-id))))

(defn remove-from-team!
  "Removes the specified user from the team with the given id."
  [group-id team-name user-id]
  (redis/srem db (team-members-key group-id team-name) user-id)
  (redis/del db (team-membership-key group-id user-id)))

(defn get-team-members
  "Returns a list of all user ids of all members of the given team."
  [group-id team-name]
  (redis/smembers db (team-members-key group-id team-name)))

(defn get-group-id-by-name [name]
  (redis/get db (group-key name)))
