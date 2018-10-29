(ns bts.components.tabs
  (:require [reagent.core :as r]))

(defn component [tabs content selected]
  (when (or (nil? @selected) (>= @selected (count tabs))) (reset! selected 0))
  (fn []
    [:div.tabs
     [:div.tabs__header
      (doall (for [t (map-indexed list tabs)]
               [:div.tabs__header__tab
                {:key      (nth t 0)
                 :class    (if (= @selected (nth t 0)) "selected" "")
                 :on-click #(do (.preventDefault %) (reset! selected (nth t 0)))}
                (nth t 1)]))]
     [:div.tabs__contents
      (doall (for [c (map-indexed list content)]
               [:div {:key (nth c 0) :class (if (= @selected (nth c 0)) "tabs__content tabs__content-selected" "tabs__content")}
                (nth c 1)]))
      ]]))