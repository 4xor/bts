(ns bts.core
  (:require [reagent.core :as r]
            [bts.components.index :as index]))

(defn ^:export main []
  (when-not (= (.-hostname js/location) "localhost")
    (when-not (= (.-protocol js/location) "https:") (set! (.-protocol js/location) "https:")))
  (r/render [index/component]
            (.getElementById js/document "app")))

(defn on-js-reload []
  (main))
