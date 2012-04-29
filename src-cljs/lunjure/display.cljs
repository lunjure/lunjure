(ns lunjure.display
  (:require [clojure.browser.dom :as dom]
            [goog.dom :as gdom]
            [clojure.browser.event :as event]))

(def jquery (js* "$"))

(def output (jquery "#text_window .output"))

;;; TODO
;; (defn format-time-string [time]
;;   (.log js/console time)
;;   (let [date (js/Date. (* time 1000))]
;;     (str (.getHours date) ":" (.getMinutes date))))
(def format-time-string identity)

;;; Team list (notepad)

(defn- make-user [obj]
  (assert (:user-id obj))
  (assert (:user obj))
  (.. (jquery "<li>")
      (addClass "user")
      (addClass (str "userId-" (:user-id obj)))
      (append (.. (jquery "<div>")
                  ;; TODO: Use foursquare avatar
                  (append (.attr (jquery "<img>")
                                "src" "/images/avatar.png"))
                  (append (:user obj))))))

(defn- make-team [name & [foursquare-id]]
  (.. (jquery "<ul>")
      (addClass "team")
      (addClass (str "team-" (hash name)))
      (append (.. (jquery "<li>")
                  (addClass "team-name")
                  (append (.text (jquery "<span>") name))
                  (append (.addClass (jquery "<ul>") "users"))))))

(defn- append-user [team-name user]
  (.. (jquery (str ".team-" (hash team-name) " .users"))
      (append user)))

(defn- get-team [name]
  (jquery (str ".team-" (hash name))))

(defn- display-in-team [user user-id team-name]
  (.log js/console user-id)
  (.remove (jquery (str ".userId-" user-id)))
  (append-user team-name (make-user {:user user
                                     :user-id user-id})))

(defn add-team [team-name]
  (.. (jquery "#text_pad .wrapper")
      (append (make-team team-name))))

(defn- test-tree []
  (add-team "Foobar")
  (doseq [n ["Foo" "bar" "Baz"]]
    (append-user "Foobar" (make-user {:user n
                                      :user-id "foobar4242"}))))

;; (def- append-team [name]
;;   (.. (jquery "<ul>")
;;       (class "team")
;;       (append (.. (jquery "<li>")
;;                   (class "team-name")))))

;;; Chat messages

(defn append-element [el]
  (dom/append (gdom/getFirstElementChild (dom/get-element "text_window"))
              el)
  (.scrollTop output (.-scrollHeight (.get output 0))))

(defmulti make-message-element :type)

;;; TODO: Change to :message
;;; TODO: data-usercolor
(defmethod make-message-element :default [obj]
  (.. (jquery "<p>")
      (attr "data-username" (:user obj))
      (attr "data-time" (format-time-string (:time-string obj)))
      (text (:text obj))))

(defmethod make-message-element :team [obj]
  ;; TODO: Team zur Liste hinzufuegen
  (.. (jquery "<p>")
      (attr "class" "status")
      (attr "data-time" (format-time-string (:time-string obj)))
      (text (str (:user obj)
                 " has created team "
                 (:name obj)
                 (if (:lunch-time obj)
                   (str " (" (:lunch-time obj) ")")
                   "")
                 "."))))

;; (defmethod make-message-element :invite [obj]
;;   (dom/element :p {"class" "status"}
;;                (str (:user obj) " hat " (:name obj) " eingeladen.")))

(defmethod make-message-element :time [obj]
  ;; TODO: Zeit aktualisieren
  (.. (jquery "<p>")
      (attr "class" "status")
      (attr "data-time" (format-time-string (:time-string obj)))
      (text (str (:user obj) " has set lunch time to "
                 ;; TODO: mention team
                 (format-time-string (:lunch-time obj))
                 "."))))

(defmethod make-message-element :leave [obj]
  ;; TODO: Aus Liste entfernen
  (.. (jquery "<p>")
      (attr "class" "status")
      (attr "data-time" (format-time-string (:time-string obj)))
      (text (str (:user obj) " has left team " (:team obj) "."))))

(defmethod make-message-element :join [obj]
  ;; TODO: Zur Liste hinzufuegen
  (.. (jquery "<p>")
      (attr "class" "status")
      (attr "data-time" (format-time-string (:time-string obj)))
      (text (str (:user obj) " has joined team " (:team obj) "."))))

(defmethod make-message-element :enter [obj]
  (.. (jquery "<p>")
      (attr "class" "status")
      (attr "data-time" (format-time-string (:time-string obj)))
      (text (str (:user obj) " has entered."))))

(defmethod make-message-element :exit [obj]
  (.. (jquery "<p>")
      (attr "class" "status")
      (attr "data-time" (format-time-string (:time-string obj)))
      (text (str (:user obj) " has left."))))

(defmethod make-message-element :geolocation [obj]
  (.. (jquery "<p>")
      (attr "class" "status")
      (attr "data-time" (format-time-string (:time-string obj)))
      (text (str (:user obj) " has changed the group's geolocation."))))
