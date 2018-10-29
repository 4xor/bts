(ns bts.admin.core
  (:require [bts.admin.router :as router]
            [reagent.core :as reagent]))

(defn ^:export main []
  (router/app-routes)
  (reagent/render [router/current-page]
                  (.getElementById js/document "app")))


(defn on-js-reload []
  (main))