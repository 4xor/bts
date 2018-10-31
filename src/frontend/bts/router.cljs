(ns bts.router
  (:require-macros [secretary.core :refer [defroute]])
  (:import goog.history.Html5History)
  (:require [secretary.core :as secretary]
            [bts.state :refer [state]]
            [bts.nav :as nav]
            [bts.components.index :as index]))

(defn app-routes []
  (secretary/set-config! :prefix "#")

  (defroute "/" [query-params]
            (swap! state assoc :q (or (:q query-params) ""))
            (swap! state assoc :sort (or (:sort-by query-params) "seed"))
            (swap! state assoc :sort-dir (or (:sort-dir query-params) "desc"))
            (swap! state assoc :page :index))

  (nav/hook-browser-navigation!))

(defmulti current-page #(@state :page))
(defmethod current-page :index []
  [index/component])
(defmethod current-page :default []
  [:div "Not found"])