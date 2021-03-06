(ns lunjure.input
  (:require [cljs.reader :as reader]
            [lunjure.logging :as logging]
            [clojure.browser.dom :as dom]
            [clojure.browser.event :as event]))

(def jquery (js* "$"))

(defmulti handle-command first)

(defn sanitize-input [text]
  (if-let [[[text pre h m post]] (re-seq #"(.*)(\d\d):(\d\d)(.*)" text)]
    (str pre "\"" h ":" m "\"" post)
    text))

(defn parse-input [text]
  (let [obj (when (= 0(.indexOf text "("))
              (try
                (reader/read-string (sanitize-input text))
                (catch Error e nil)))]
    (if (or (nil? obj) (not (seq? obj)))
      {:type :message
       :text text}
      (handle-command (map str obj) text))))

(defmethod handle-command :default [[command & _] text]
  (logging/log "Got unknown command: " command)
  {:type :message
   :text text})

(defmethod handle-command "team" [[_ team time location] text]
  {:type :team
   :name  team
   :lunch-time  time
   :location location
   :text  text})

(defmethod handle-command "join" [[_ team] text]
  {:type :join
   :team team
   :text text})

(defmethod handle-command "leave" [_ text]
  {:type :leave
   :text text})

;; (defmethod handle-command "time" [[_ time] text]
;;   {:type :time
;;    :lunch-time time
;;    :text text})

;; (defmethod handle-command "invite" [[_ name] text]
;;   {:type :invite
;;    :name name
;;    :text text})

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
  (when-let [[_ cmd alias time location]
             (re-find #"^\(([^ ]+)\s+([^ ]+)\s+([^ ]+)\s+([^ ]+)" text)]
    [cmd alias time location]))

(defn autocomplete-should-complete [event ui]
  (let [el (.-currentTarget event)
        text (.-value el)]
    (when-let [[cmd alias time location] (simple-parse-command text)]
      (logging/log "asdf" location)
      (boolean location))))

(defn data-provider [request response]
  (let [term (.-term request)
        [cmd alias time location] (simple-parse-command term)]
    (when location
      (-> jquery
          (.getJSON (str (-> js/window .-location .-pathname)
                         "/locations?term=" (js/encodeURIComponent location))
                    nil
                    (fn [data status xhr]
                      (logging/log "cmd: " data)
                      (response (apply array (map #(str "(" cmd " " alias " " time " " %) data))))))) ))

(jquery (fn []
          (-> (jquery "#message")
              (.autocomplete (clj->js {:source data-provider
                                       ;; :search autocomplete-should-complete
                                       })))))
