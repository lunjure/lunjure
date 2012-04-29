(ns lunjure.display
  (:require [clojure.browser.dom :as dom]
            [goog.dom :as gdom]
            [clojure.browser.event :as event]))

(def jquery (js* "$"))

(def output (jquery "#text_window .output"))

(def emojis
  {#{":-)" ":)"} "smile"
   #{";-)" ";)"} "blink"
   #{":-D" ":D"} "bigSmile"
   #{";-D" ";D"} "bigBlink"
   #{";-P" ";P"} "blinkTongue"
   #{":-P" ":P"} "tongue"
   #{":-X" ":X"} "iDontGiveAFuck"
   #{":-O" ":O"} "O"
   #{":-(" ":("} "sad"
   #{":love:" "&lt;3"} "love"
   "+1" "plusOne"
   "-1" "minusOne"
   ":burger:" "burger"
   ":cry:" "cry"
   ":hehe:" "hehe"
   ":ice:" "ice"
   ":rain:" "rain"
   ":sun:" "sun"
   ":wtf:" "whatTheFuck"
   ":zzz:" "zzz"})

(defn emojify [text]
  (let [text (.. (jquery "<div>")
                 (text text)
                 (get 0)
                 -innerHTML)]
    (reduce (fn [text [emoji-names emoji-img]]
              (let [img (.. (jquery "<img>")
                            (addClass "emoticon")
                            (attr "src" (str "/images/emoticons/" emoji-img ".png"))
                            (get 0)
                            -outerHTML)]
                (.log js/console (str "ho: " img))
                (reduce (fn [text emoji]
                          (.log js/console text)
                          (.replace text emoji img))
                        text
                        (if (set? emoji-names)
                          emoji-names
                          [emoji-names]))))
            text
            emojis)))

;;; TODO
;; (defn format-time-string [time]
;;   (.log js/console time)
;;   (let [date (js/Date. (* time 1000))]
;;     (str (.getHours date) ":" (.getMinutes date))))
(def format-time-string identity)

;;; Team list (notepad)

(defn sanitize-avatar-url [url]
  (when (= -1 (.indexOf url "blank_"))
    url))

(defn- make-user [obj]
  (assert (:user-id obj))
  (assert (:user obj))

  (let [photo-url (sanitize-avatar-url (:user-photo obj))]
   (.. (jquery "<li>")
       (addClass "user")
       (addClass (when photo-url "avatar"))
       (addClass (str "userId-" (:user-id obj)))
       (append (.. (jquery "<div>")
                   (append (.attr (jquery "<img>")
                                  "src" (if photo-url
                                          photo-url
                                          "/images/avatar.png")))
                   (append (:user obj)))))))

(defn- make-team [name lunch-time]
  (.log js/console (str "time: " lunch-time))
  (.. (jquery "<ul>")
      (addClass "team")
      (addClass (str "team-" (hash name)))
      (append (.. (jquery "<li>")
                  (addClass "team-name")
                  (append (.. (jquery "<span>")
                              (attr "data-lunchtime" lunch-time)
                              (text name)))
                  (append (.addClass (jquery "<ul>") "users"))))))

(defn- append-user [team-name user]
  (.. (jquery (str ".team-" (hash team-name) " .users"))
      (append user)))

(defn- get-team [name]
  (jquery (str ".team-" (hash name))))

(defn get-user [user-id]
  (jquery (str ".userId-" user-id)))

(defn user-in-team? [user-id team-name]
  (let [user (first (.makeArray jquery (get-user user-id)))
        tn (-> user
               (jquery)
               (.parents ".team")
               (.children ".team-name")
               (.children "span")
               (.text))]
    (= team-name tn)))

(defn- display-in-team [user user-id team-name & [photo-id]]
  (.log js/console user-id)
  (.remove (jquery (str ".userId-" user-id)))
  (append-user team-name (make-user {:user user
                                     :user-id user-id
                                     :user-photo photo-id})))

(defn add-team [team-name lunch-time]
  (.. (jquery "#text_pad .wrapper")
      (append (make-team team-name lunch-time))))

;; (def- append-team [name]
;;   (.. (jquery "<ul>")
;;       (class "team")
;;       (append (.. (jquery "<li>")
;;                   (class "team-name")))))

;;; Chat messages

(defn append-element [el]
  (dom/append (gdom/getFirstElementChild (dom/get-element "text_window"))
              el)
  (.scrollTop output (.-scrollHeight (.get output 0))))

(defmulti make-message-element :type)

;;; TODO: Change to :message
;;; TODO: data-usercolor
(defmethod make-message-element :default [obj]
  (.. (jquery "<p>")
      (attr "data-username" (:user obj))
      (attr "data-time" (format-time-string (:time-string obj)))
      (html (emojify (:text obj)))))

(defmethod make-message-element :team [obj]
  (add-team (:name obj) (:lunch-time obj))
  (display-in-team (:user obj)
                   (:user-id obj)
                   (:name obj)
                   (:user-photo obj))

  (.. (jquery "<p>")
      (attr "class" "status")
      (attr "data-time" (format-time-string (:time-string obj)))
      (text (str (:user obj)
                 " has created team "
                 (:name obj)
                 (if (:lunch-time obj)
                   (str " (" (:lunch-time obj) ")")
                   "")
                 "."))))

;; (defmethod make-message-element :invite [obj]
;;   (dom/element :p {"class" "status"}
;;                (str (:user obj) " hat " (:name obj) " eingeladen.")))

(defmethod make-message-element :time [obj]
  (.. (jquery "<p>")
      (attr "class" "status")
      (attr "data-time" (format-time-string (:time-string obj)))
      (text (str (:user obj) " has set lunch time to "
                 ;; TODO: mention team
                 (format-time-string (:lunch-time obj))
                 "."))))

(defmethod make-message-element :leave [obj]
  (display-in-team (:user obj)
                   (:user-id obj)
                   "Undecided"
                   (:user-photo obj))

  (.. (jquery "<p>")
      (attr "class" "status")
      (attr "data-time" (format-time-string (:time-string obj)))
      (text (str (:user obj) " has left team " (:team obj) "."))))

(defmethod make-message-element :join [obj]
  (display-in-team (:user obj)
                   (:user-id obj)
                   (:team obj)
                   (:user-photo obj))

  (.. (jquery "<p>")
      (attr "class" "status")
      (attr "data-time" (format-time-string (:time-string obj)))
      (text (str (:user obj) " has joined team " (:team obj) "."))))

(defmethod make-message-element :enter [obj]
  ;; Add user to the "Undecided" group
  ;; TODO: Check if user is already in a group
  (when (or (= 0 (.-length (get-user (:user-id obj))))
            (user-in-team? (:user-id obj) "Undecided"))
    (.log js/console "We are IN 'Undecided'")
    (display-in-team (:user obj)
                     (:user-id obj)
                     "Undecided"
                     (:user-photo obj)))

  (.. (jquery "<p>")
      (attr "class" "status")
      (attr "data-time" (format-time-string (:time-string obj)))
      (text (str (:user obj) " has entered."))))

(defmethod make-message-element :exit [obj]
  (.. (jquery "<p>")
      (attr "class" "status")
      (attr "data-time" (format-time-string (:time-string obj)))
      (text (str (:user obj) " has left."))))

(defmethod make-message-element :geolocation [obj]
  (.. (jquery "<p>")
      (attr "class" "status")
      (attr "data-time" (format-time-string (:time-string obj)))
      (text (str (:user obj) " has changed the group's geolocation."))))

;;; Add an "undecided" team
(add-team "Undecided" "")
