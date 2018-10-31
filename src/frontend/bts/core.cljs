(ns bts.core
  (:require [reagent.core :as r]
            [bts.components.index :as index]
            [bts.router :as router]))

(defn ^:export main []
  (when-not (= (.-hostname js/location) "localhost")
    (when-not (= (.-protocol js/location) "https:") (set! (.-protocol js/location) "https:")))
  (router/app-routes)
  (r/render [router/current-page]
            (.getElementById js/document "app")))

(defn on-js-reload []
  (main))
