(ns chess.client.remote
  (:require [goog.net.XhrIo]))

;; see https://gist.github.com/3153856
;; borrowed from ibdknox/jayq
(defn map->js [m]
  (let [out (js-obj)]
    (doseq [[k v] m]
      (aset out (name k) v))
    out))




(defn- keywordize-rochade-vec [state]
  (update-in state [:rochade] #(vec (map keyword %))))

(defn- keywordize-board-vec [state]
  (update-in state [:board] #(vec (map (fn [row]
                                         (vec (map keyword row)))
                                       %))))

(defn- keywordize-turn [state]
  (update-in state [:turn] keyword))

(defn- parse-json-gamestate [json]
  (-> json
      (js->clj :keywordize-keys true)
      keywordize-rochade-vec
      keywordize-board-vec
      keywordize-turn))

(defn get-gamestate [id callback]
  (let [url (str "/gamestates/" id)]
    (.send goog.net.XhrIo
           url
           (fn [event]
             (-> event
                 .-target
                 .getResponseJson
                 parse-json-gamestate
                 callback)))))

(defn move [id move callback]
  (let [url (str "/gamestates/" id "/move")
        json (clj->js move)]
    (.send goog.net.XhrIo
           url
           (fn [event]
             (let [target (.-target event)]
               (if (= 200 (.getStatus target))
                 (-> target
                     .getResponseJson
                     parse-json-gamestate
                     callback)
                 (callback nil))))
           "POST"
           (.serialize goog.json json)
           (clj->js {"Content-Type" "application/json"}))))

(defn nxt [id callback]
  (let [url (str "/gamestates/" id "/next")]
    (.send goog.net.XhrIo
           url
           (fn [event]
             (-> event
                 .-target
                 .getResponseJson
                 parse-json-gamestate
                 callback))
           "POST")))
