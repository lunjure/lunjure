(ns lunjure.display
  (:require [clojure.browser.dom :as dom]
            [goog.dom :as gdom]
            [clojure.browser.event :as event]))

(def jquery (js* "$"))

;;; TODO
;; (defn format-time-string [time]
;;   (.log js/console time)
;;   (let [date (js/Date. (* time 1000))]
;;     (str (.getHours date) ":" (.getMinutes date))))
(def format-time-string identity)

;;; Team list (notepad)



;;; Chat messages

(defn append-element [el]
  (dom/append (gdom/getFirstElementChild (dom/get-element "text_window"))
              el))

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
      (text (str (:user obj) " das Team " (:name obj) " erstellt."))))

;; (defmethod make-message-element :invite [obj]
;;   (dom/element :p {"class" "status"}
;;                (str (:user obj) " hat " (:name obj) " eingeladen.")))

(defmethod make-message-element :time [obj]
  ;; TODO: Zeit aktualisieren
  (.. (jquery "<p>")
      (attr "class" "status")
      (text (str (:user obj) " hat die Uhrzeit auf "
                 ;; TODO: :lunch-time instead of :time-string
                 (format-time-string (:time-string obj)) " gesetzt."))))

(defmethod make-message-element :leave [obj]
  ;; TODO: Aus Liste entfernen
  (.. (jquery "<p>")
      (attr "class" "status")
      (text (str (:user obj) " hat das Team " (:team obj) " verlassen."))))

(defmethod make-message-element :join [obj]
  ;; TODO: Zur Liste hinzufuegen
  (.. (jquery "<p>")
      (attr "class" "status")
      (text (str (:user obj) " ist dem Team " (:team obj) " beigetreten."))))

(defmethod make-message-element :enter [obj]
  (.. (jquery "<p>")
      (attr "class" "status")
      (text (str (:user obj) " has entered the Group."))))

(defmethod make-message-element :geolocation [obj]
  (.. (jquery "<p>")
      (attr "class" "status")
      (text (str (:user obj) " changed the geolocation of this Group."))))
