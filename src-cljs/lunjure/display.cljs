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
