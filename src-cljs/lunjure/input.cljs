(ns lunjure.input
  (:require [cljs.reader :as reader]
            [lunjure.logging :as logging]
            [clojure.browser.dom :as dom]
            [clojure.browser.event :as event]
            [goog.ui.AutoComplete.Remote :as Remote]))

(defmulti handle-command first)

(defn parse-input [text]
  (let [obj (try
              (reader/read-string text)
              (catch Error e nil))]
    (logging/log obj)
    (if (or (nil? obj) (not (seq? obj)))
      {:type :message
       :text text}
      (handle-command (map str obj) text))))

(defmethod handle-command :default [[command & _] _]
  (logging/log "Got unknown command: " command))

(defmethod handle-command "defteam" [[_ name time alias] text]
  {:type :defteam
   :name name
   :time time
   :alias alias
   :text text})

;;; Completion Stuff

(defn on-input-change [event]
  (logging/log "change:" (-> event .-currentTarget .-value)))

(let [el (dom/get-element :message)]
  (event/listen el "keyup" on-input-change))
