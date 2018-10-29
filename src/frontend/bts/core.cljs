
(ns bts.core
  (:require [reagent.core :as r]
            [bts.components.index :as index]))

(defn ^:export  main []
  (r/render [index/component]
            (.getElementById js/document "app")))

(defn on-js-reload []
  (main))
