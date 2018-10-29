(ns user
  (:require [bts.worker.rutracker :as rt]
            [bts.db :refer [db migrate]]
            [figwheel-sidecar.repl-api :as fw]
            [bts.components.scheduler :as scheduler]))

(migrate)
(fw/start-figwheel! "dev" "admin-dev")
(scheduler/start db)
(rt/start-topic-worker db)

