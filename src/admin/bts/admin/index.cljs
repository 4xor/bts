(ns bts.admin.index
  (:use-macros [cljs.core.async.macros :only [go]])
  (:require [cljs.core.async :refer [<! timeout]]
            [bts.admin.api :as api]
            [reagent.core :as r]
            [bts.admin.state :as state]))

(defn- header-link-class [name]
  (let [p @(state/page)]
    (if (= p name) "active" "")))

(defn- task-stats-table [data]
  [:table
   [:thead
    [:tr
     [:th "Type"]
     [:th "Waiting"]
     [:th "Complete"]]]
   [:tbody
    (for [r @data]
      [:tr {:key (:task-type r)}
       [:td (:task-type r)]
       [:td (:waiting-count r)]
       [:td (:complete-count r)]])
    ]])

(defn- torrent-stats-table [data]
  [:table
   [:thead
    [:tr
     [:th "Source"]
     [:th "Count"]]]
   [:tbody
    (for [r @data]
      [:tr {:key (:source r)}
       [:td (:source r)]
       [:td (:cnt r)]])
    ]])

(defn layout [& args]
  [:div
   [:div.header
    [:h1.header__title "[BTS] Admin Panel"]
    [:div.header__links
     [:a.header__links__link {:href "#/" :class (header-link-class :index)} [:span "Dashboard"]]
     [:a.header__links__link {:href "#/schedulers" :class (header-link-class :scheduler)} [:span "Scheduler"]]]]
   (into [:div.content] args)])

(defn component []
  (go (let [tr (<! (api/dashboard-stats-tasks))
            tor (<! (api/dashboard-stats-torrents))]
        (when (nil? (:error tr)) (state/dashboard-task-stats! tr))
        (when (nil? (:error tor)) (state/dashboard-torrent-stats! tor))))
  (fn []
    [layout
     [:div.row
      [:div.col
       [:h3 "Task stats"]
       [task-stats-table (state/dashboard-task-stats)]]
      [:div.col
       [:h3 "Torrent stats"]
       [torrent-stats-table (state/dashboard-torrent-stats)]]]]))