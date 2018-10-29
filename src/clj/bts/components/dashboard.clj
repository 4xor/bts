(ns bts.components.dashboard
  (:require [hugsql.core :as hugsql]
            [compojure.core :refer :all]
            [ring.util.response :refer [response]]
            [bts.utils :as utils]))


(hugsql/def-db-fns "sql/dashboard.sql")

(defn handler [db]
  (routes
    (GET "/stats-tasks" [] (utils/json (stats-tasks db)))
    (GET "/stats-torrents" [] (utils/json (stats-torrents db)))))