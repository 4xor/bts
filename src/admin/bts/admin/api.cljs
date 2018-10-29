(ns bts.admin.api
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [chan timeout go put! <!]]))

(defn- map-api-response [{success :success body :body}]
  (if success body {:error body}))

(defn schedule-new [name action-name params description cron]
  (http/post "/api/admin/v1/schedulers"
             {:json-params {:name        name
                            :action-name action-name
                            :params      params
                            :description description
                            :cron        cron}
              :channel     (chan 1 (map map-api-response))}))

(defn schedule-update [name action-name params description cron]
  (http/post (str "/api/admin/v1/schedulers/" name)
             {:json-params {:action-name action-name
                            :params      params
                            :description description
                            :cron        cron}
              :channel     (chan 1 (map map-api-response))}))

(defn schedule-list []
  (http/get "/api/admin/v1/schedulers" {:channel (chan 1 (map map-api-response))}))

(defn schedule-info [name]
  (http/get (str "/api/admin/v1/schedulers/" name) {:channel (chan 1 (map map-api-response))}))

(defn schedule-start [name]
  (http/get (str "/api/admin/v1/schedulers/" name "/start") {:channel (chan 1 (map map-api-response))}))

(defn schedule-delete [name]
  (http/delete (str "/api/admin/v1/schedulers/" name) {:channel (chan 1 (map map-api-response))}))

(defn dashboard-stats-tasks []
  (http/get "/api/admin/v1/dashboard/stats-tasks" {:channel (chan 1 (map map-api-response))}))

(defn dashboard-stats-torrents []
  (http/get "/api/admin/v1/dashboard/stats-torrents" {:channel (chan 1 (map map-api-response))}))