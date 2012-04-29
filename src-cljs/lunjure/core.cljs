(ns lunjure.core
  (:require [lunjure.socket :as socket]
            [lunjure.input :as input]
            [clojure.browser.dom :as dom]
            [clojure.browser.event :as event]))

(defmethod input/handle-command "geolocation" [_ text]
  (-> js/navigator
      .-geolocation
      (.getCurrentPosition (fn success [pos]
                             (let [c (.-coords pos)]
                              (socket/send-data {:type :geolocation
                                                 :latitude  (.-latitude  c)
                                                 :longitude (.-longitude c)
                                                 :accuracy  (.-accuracy  c)})))
                           (fn error [pos]
                             (.log js/console "Failed to update geolocation."))
                           (js* "{maximumAge:600000}"))))

(event/listen (dom/get-element "message")
              "keydown"
              (fn [ev]
                (when (== 13 (.-keyCode ev))
                  (let [el (dom/get-element "message")
                        text (dom/get-value el)
                        msg (input/parse-input text)]
                    (socket/send-data msg)
                    (dom/set-value el "")
                    (.focus (dom/get-element "message"))))))



(let [location (.-location js/window)]
  (socket/open-socket (str "ws://"
                           (.-host location)
                           (.-pathname location)
                           "/socket")))

(.focus (dom/get-element "message"))
