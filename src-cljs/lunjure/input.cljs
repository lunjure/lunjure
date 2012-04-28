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

(defn simple-parse-command [text]
  (when-let [[_ cmd location] (re-find #"^\(([^ ]+) ([^ ]+)" text)]
    [cmd location]))

(defn autocomplete-should-complete [event ui]
  (let [el (.-currentTarget event)
        text (.-value el)]
    (when-let [[cmd location] (simple-parse-command text)]
      (logging/log "asdf" location)
      (boolean location))))

(defn data-provider [request response]
  (let [term (.-term request)
        [cmd location] (simple-parse-command term)]
    (when location
     (-> jquery
         (.getJSON (str "/locations?term=" location)
                   nil
                   (fn [data status xhr]
                     (logging/log "cmd: " data)
                     (response (apply array (map #(str "(" cmd " " %) data))))))) ))

(jquery (fn []
          (-> (jquery "#message")
              (.autocomplete (clj->js {:source data-provider
                                       ;; :search autocomplete-should-complete
                                       })))))
