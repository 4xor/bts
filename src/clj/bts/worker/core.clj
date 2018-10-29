(ns bts.worker.core
  (:require [cheshire.core :as json]
            [clojure.tools.logging :as log]
            [hugsql.core :as hugsql]))

(hugsql/def-db-fns "sql/queue.sql")

(defn thread [f]
  (let [t (Thread. (fn [] (f)))]
    (.start t)
    t))

(defn schedule [db task-type params]
  (let [ret (insert-task db {:type task-type
                                   :params (json/generate-string params)})]
    (:id (first ret))))

(defn start [db task-type f]
  (thread #(while (not (Thread/interrupted))
             (try (if-let [task (first (take-task db {:type task-type}))]
                    (let [id (:id task)
                          params (json/parse-string (:params task) true)
                          res (f id params)]
                      (log/debug "Start execute task [" task-type "][" id "] with " params)
                      (complete-task db {:id id :result (json/generate-string res)}))
                    (Thread/sleep 500))
                  (catch Exception e (log/error e "Exception on task worker"))))))

(defn stop [w]
  (.interrupt w)
  (.join w))
