(ns lunjure.http
  (:use ring.util.response
        clojure.data.json))

(defn html-response [body]
  (-> (response body)
      (content-type "text/html")))

(defn json-response [data]
  (-> (json-str data)
      (response)
      (content-type "application/json")))
