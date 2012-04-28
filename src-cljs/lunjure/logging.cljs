(ns lunjure.logging
  (:require [clojure.browser.dom :as dom]))

(defn log [& more]
  (.log js/console (apply str more)))
