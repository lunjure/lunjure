(ns lunjure.core
  (:require [lunjure.socket :as socket]
            [lunjure.input :as input]
            [clojure.browser.dom :as dom]
            [clojure.browser.event :as event]))

(event/listen (dom/get-element "message")
              "keydown"
              (fn [ev]
                (when (== 13 (.-keyCode ev))
                  (let [el (dom/get-element "message")
                        text (dom/get-value el)
                        msg (input/parse-input text)]
                    (socket/send-data msg)
                    (dom/set-value el "")))))

(let [location (.-location js/window)]
  (socket/open-socket (str "ws://"
                           (.-host location)
                           (.-pathname location)
                           "/socket")))
