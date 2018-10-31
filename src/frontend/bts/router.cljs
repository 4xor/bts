(ns bts.router
  (:require-macros [secretary.core :refer [defroute]])
  (:import goog.history.Html5History)
  (:require [secretary.core :as secretary]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [reagent.core :as reagent]
            [bts.state :refer [state]]
            [bts.components.index :as index]))

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

(defn app-routes []
  (secretary/set-config! :prefix "#")

  (defroute "/" [query-params]
            (swap! state assoc :q (or (:q query-params) ""))
            (swap! state assoc :sort (or (:sort-by query-params) "seed"))
            (swap! state assoc :sort-dir (or (:sort-dir query-params) "desc"))
            (swap! state assoc :page :index))

  (hook-browser-navigation!))

(defmulti current-page #(@state :page))
(defmethod current-page :index []
  [index/component])
(defmethod current-page :default []
  [:div "Not found"])

(defn goto [uri]
  (.setToken @html5-history uri nil))