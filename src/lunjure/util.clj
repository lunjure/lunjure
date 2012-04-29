(ns
    ^{:doc "Some commonly used utility functions."}
  lunjure.util)

(defn new-uuid
  "Returns a new UUID as a string."
  []
  (str (java.util.UUID/randomUUID)))

(defn minutes [n] (* n 60))

(defn hours [n] (* n (minutes 60)))

(defn days [n] (* n (hours 24)))

(defn weeks [n] (* n (days 7)))

(defn now []
  (System/currentTimeMillis))
