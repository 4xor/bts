(ns bts.nav
  (:import goog.history.Html5History)
  (:require [secretary.core :as secretary]
            [goog.events :as events]
            [goog.history.EventType :as EventType]))


(defonce html5-history (atom nil))

(defn hook-browser-navigation! []
  (let [h (Html5History.)]
    (reset! html5-history h)
    (doto h
      (events/listen
        EventType/NAVIGATE
        (fn [event]
          (secretary/dispatch! (.-token event))))
      (.setEnabled true))))

(defn goto [uri]
  (.setToken @html5-history uri nil))