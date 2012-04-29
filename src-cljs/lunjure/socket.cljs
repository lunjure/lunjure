(ns lunjure.socket
  (:require [cljs.reader :as reader]
            ;; TODO: move to core
            [lunjure.display :as display]))

(def jquery (js* "$"))

(def *socket* nil)

(defn send-data [data]
  (binding [*print-meta* true]
    (.log js/console (pr-str data))
    (.send *socket* (pr-str data))))

(defn handle-close []
  (.. (jquery "#chat_window")
      (removeClass "active")))

(defn handle-open []
  (.. (jquery "#chat_window")
      (addClass "active")))

(defn handle-socket-message [socket-event]
  (let [obj (reader/read-string (.-data socket-event))]
    (assert (map? obj))
    (assert (keyword? (:type obj)))
    (let [el (display/make-message-element obj)]
      (.log js/console el)
     (display/append-element el))))

(defn ^:export open-socket [uri]
  (let [ws (if (.-MozWebSocket js/window) (js/MozWebSocket. uri) (js/WebSocket. uri))]
    (set! (.-onopen ws)    (fn [_] (handle-open)))
    (set! (.-onclose ws)   (fn [ ] (handle-close)))
    (set! (.-onmessage ws) #(handle-socket-message %))
    (set! *socket* ws)))
