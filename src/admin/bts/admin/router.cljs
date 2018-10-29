(ns bts.admin.router
  (:require-macros [secretary.core :refer [defroute]])
  (:import goog.history.Html5History)
  (:require [secretary.core :as secretary]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [reagent.core :as reagent]
            [bts.admin.state :refer [state]]
            [bts.admin.index :as index]
            [bts.admin.scheduler :as scheduler]))

(defn hook-browser-navigation! []
  (doto (Html5History.)
    (events/listen
      EventType/NAVIGATE
      (fn [event]
        (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

(defn app-routes []
  (secretary/set-config! :prefix "#")

  (defroute "/" []
            (swap! state assoc :page :index))

  (defroute "/schedulers" []
            (swap! state assoc :page :scheduler))

  (hook-browser-navigation!))

(defmulti current-page #(@state :page))
(defmethod current-page :index []
  [index/component])
(defmethod current-page :scheduler []
  [scheduler/component])
(defmethod current-page :default []
  [:div "Not found"])