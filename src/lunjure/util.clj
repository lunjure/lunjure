(ns
    ^{:doc "Some commonly used utility functions."}
  lunjure.util)

(defn new-uuid
  "Returns a new UUID as a string."
  []
  (str (java.util.UUID/randomUUID)))

(defn now []
  (long (/ (System/currentTimeMillis) 1000)))
