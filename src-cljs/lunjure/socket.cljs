(ns lunjure.socket
  (:require [cljs.reader :as reader]))

(def *socket* nil)

(defn send-data [data]
  (binding [*print-meta* true]
    (.send *socket* (pr-str data))))

(defn handle-close [])
(defn handle-open [])

(defn handle-socket-message [socket-event]
  (let [obj (reader/read-string (.-data socket-event))]
    (assert (map? obj))
    (assert (keyword? (:type obj)))
    (.log js/console obj)))

(defn ^:export open-socket [uri]
  (let [ws (if (.-MozWebSocket js/window) (js/MozWebSocket. uri) (js/WebSocket. uri))]
    (set! (.-onopen ws)    (fn [_] (handle-open)))
    (set! (.-onclose ws)   (fn [ ] (handle-close)))
    (set! (.-onmessage ws) #(handle-socket-message %))
    (set! *socket* ws)))
