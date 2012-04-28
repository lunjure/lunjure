(ns lunjure.display
  (:require [clojure.browser.dom :as dom]
            [goog.dom :as gdom]
            [clojure.browser.event :as event]))

(defn append-element [el]
  (dom/append (gdom/getFirstElementChild (dom/get-element "text_window"))
              el))

(defmulti make-message-element :type)

;;; TODO: Change to :message
(defmethod make-message-element :default [obj]
  (dom/element :p {"title" (:user obj)}
               (:text obj)))

(defmethod make-message-element :team [obj]
  ;; TODO: Team zur Liste hinzufuegen
  (dom/element :p (str (:user obj) " das Team " (:name obj) " erstellt.")))

(defmethod make-message-element :invite [obj]
  (dom/element :p (str (:user obj) " hat " (:name obj) " eingeladen.")))

;;; TODO
(def parse-time identity)

(defmethod make-message-element :time [obj]
  ;; TODO: Zeit aktualisieren
  (dom/element :p (str (:user obj) " hat die Uhrzeit auf " (parse-time (:time obj)) " gesetzt.")))

(defmethod make-message-element :leave [obj]
  ;; TODO: Aus Liste entfernen
  (dom/element :p (str (:user obj) " hat das Team " (:team obj) " verlassen.")))

(defmethod make-message-element :join [obj]
  ;; TODO: Zur Liste hinzufuegen
  (dom/element :p (str (:user obj) " ist dem Team " (:team obj) " beigetreten.")))
