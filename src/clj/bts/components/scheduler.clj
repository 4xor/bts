(ns bts.components.scheduler
  (:require [hugsql.core :as hugsql]
            [compojure.core :refer :all]
            [ring.util.response :refer [response]]
            [clojurewerkz.quartzite.scheduler :as qs]
            [clojurewerkz.quartzite.conversion :as qc]
            [clojurewerkz.quartzite.triggers :as t]
            [clojurewerkz.quartzite.jobs :as j]
            [clojurewerkz.quartzite.jobs :refer [defjob]]
            [clojurewerkz.quartzite.schedule.cron :refer [schedule cron-schedule]]
            [clojure.edn :as edn]
            [clojure.tools.logging :as log]
            [cheshire.core :as json]
            [bts.components.schedulers.core :as schedulers]
            [bts.utils :as utils]))

(hugsql/def-db-fns "sql/scheduler.sql")

(defonce ^:private quartz-system (atom nil))

(defjob ScheduledJob
        [ctx]
        (let [m (qc/from-job-data ctx)
              action (get m "action")
              params (edn/read-string (get m "params"))
              name (get m "name")
              db (get m "db")]
          (log/info "Start job" action "(" params ")")
          (start-execute-job db {:name name})
          (let [ret (schedulers/execute-schedule-action db action params)
                status (if (nil? (:error ret)) "idle" "error")]
            (if (= status "idle") (log/info "Job complete" action "(" params ") - " ret)
                                  (log/error "Job complete" action "(" params ") - " ret))
            (complete-execute-job db {:name name :result (json/generate-string ret) :status status}))))

(defn delete-job [{name :name}]
  (qs/delete-trigger @quartz-system (t/key (str "trigger." name)))
  (qs/delete-job @quartz-system (j/key (str "job." name))))

(defn setup-job [db job-info]
  (let [job (j/build
              (j/of-type ScheduledJob)
              (j/using-job-data {:name   (:name job-info)
                                 :action (:action_name job-info)
                                 :params (:params job-info)
                                 :db     db})
              (j/with-identity (j/key (str "job." (:name job-info)))))
        trigger (t/build
                  (t/with-identity (t/key (str "trigger." (:name job-info))))
                  (t/start-now)
                  (t/with-schedule (schedule
                                     (cron-schedule (:cron job-info)))))]
    (qs/schedule @quartz-system job trigger)))

(defn start [db]
  (let [jobs (find-all db)
        s (-> (qs/initialize) qs/start)]
    (reset! quartz-system s)
    (doseq [job-info jobs] (setup-job db job-info))))

(defn handler [db]
  (routes
    (GET "/" [] (utils/json (find-all db)))
    (POST "/" [name action-name params description cron]
      (let [n (->> {:name        name
                    :action_name action-name
                    :params      params
                    :description description
                    :cron        cron}
                   (insert-scheduler db)
                   (first))]
        (setup-job db n)
        (utils/json n)))
    (GET "/:name" [name] (utils/json (find-one db {:name name})))
    (POST "/:name" [name action-name params description cron]
      (let [n (->> {:name        name
                    :action_name action-name
                    :params      params
                    :description description
                    :cron        cron}
                   (update-scheduler db)
                   (first))]
        (delete-job n)
        (setup-job db n)
        (utils/json n)))
    (DELETE "/:name" [name] (do
                              (delete-job {:name name})
                              (delete-scheduler db {:name name})
                              (response {:ok true})))
    (GET "/:name/start" [name] (do
                                 (qs/trigger @quartz-system
                                             (j/key (str "job." name)))
                                 (response {:ok true})))))