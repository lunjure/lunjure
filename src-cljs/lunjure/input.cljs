(ns lunjure.input
  (:require [cljs.reader :as reader]
            [lunjure.logging :as logging]
            [clojure.browser.dom :as dom]
            [clojure.browser.event :as event]))

(def jquery (js* "$"))

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

(defn clj->js
  "Recursively transforms ClojureScript maps into Javascript objects,
   other ClojureScript colls into JavaScript arrays, and ClojureScript
   keywords into JavaScript strings.

   Borrowed and updated from mmcgrana."
  [x]
  (cond
    (string? x) x
    (keyword? x) (name x)
    (map? x) (.-strobj (reduce (fn [m [k v]]
               (assoc m (clj->js k) (clj->js v))) {} x))
    (coll? x) (apply array (map clj->js x))
    :else x))


;;; Completion Stuff

;; (defn on-input-change [event]
;;   (let [el (.-currentTarget event)
;;         text (.-value el)]
;;     (if (not= 0 (.indexOf text "(defteam"))
;;       (logging/log "Parsing command..." text))))

(defn autocomplete-search-event [event ui]
  (let [el (.-currentTarget event)
        text (.-value el)]
    (when-let [[_ cmd arg] (re-find #"^\(([^ ]+) (.+)" text)]
      (logging/log "cmd: " cmd))))

(jquery (fn []
          (-> (jquery "#message")
              (.autocomplete (clj->js {:source "/locations"
                                       :search autocomplete-search-event})))))
