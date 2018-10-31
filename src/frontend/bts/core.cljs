(ns bts.core
  (:require [reagent.core :as r]
            [bts.components.index :as index]
            [bts.router :as router]))

(defn ^:export main []
  (router/app-routes)
  (r/render [router/current-page]
            (.getElementById js/document "app")))

(defn on-js-reload []
  (main))
