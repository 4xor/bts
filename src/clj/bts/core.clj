(ns bts.core
  (:require [bts.db :refer [db migrate]]
            [bts.worker.rutracker :as rt]
            [environ.core :refer [env]]
            [aleph.http :as http]
            [bts.web :as web]
            [bts.components.scheduler :as scheduler])
  (:gen-class))

(def port (env :port "8080"))
(def twc (Integer/parseInt (env :count-topic-workers "1")))

(defn -main [& args]
  (migrate)
  (scheduler/start db)
  (dotimes [_ twc] (rt/start-topic-worker db))
  (http/start-server web/handler {:port (Integer/parseInt port)}))
