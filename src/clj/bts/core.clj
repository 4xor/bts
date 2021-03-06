(ns bts.core
  (:require [bts.db :refer [db migrate]]
            [bts.worker.rutracker :as rt]
            [environ.core :refer [env]]
            [aleph.http :as http]
            [bts.web :as web]
            [bts.components.scheduler :as scheduler])
  (:gen-class)
  (:import (java.net InetSocketAddress)))

(def port (env :port "8080"))
(def host (env :host "127.0.0.1"))
(def twc (Integer/parseInt (env :count-topic-workers "1")))
(def enable-scheduler (= (env :enable-scheduler "true") "true"))

(defn -main [& args]
  (migrate)
  (when enable-scheduler (scheduler/start db))
  (dotimes [_ twc] (rt/start-topic-worker db))
  (http/start-server web/handler {:socket-address (InetSocketAddress. host (Integer/parseInt port))}))
