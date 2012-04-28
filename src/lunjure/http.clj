(ns lunjure.http
  (:use ring.util.response))

(defn html-response [body]
  (-> (response body)
      (content-type "text/html")))
