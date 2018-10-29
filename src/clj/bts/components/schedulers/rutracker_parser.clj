(ns bts.components.schedulers.rutracker-parser
  (:require [bts.grab.rutracker :as gr]
            [clojure.tools.logging :as log]
            [bts.worker.rutracker :as rutracker]
            [hugsql.core :as hugsql]))

(hugsql/def-db-fns "sql/torrent.sql")

(defn- list-of-new-topic [db id skip]
  (let [ids (gr/topics id skip)]
    {:topics ids
     :new    (filter #(= {:r false} (torrent-exists db {:source "rutracker" :source_id %})) ids)}))

(defn- walk-forum-pages [db {id :id type :type :as params} skip collected]
  (log/debug "Walk forum page" id "[" skip "][" collected "]")
  (let [{topics :topics to-schedule :new} (list-of-new-topic db id skip)]
    (doseq [id to-schedule] (rutracker/schedule db id type))
    (if (or (empty? topics) (>= collected 10000) (< (count topics) 50))
      (+ collected (count to-schedule))
      (walk-forum-pages db params (+ skip (count topics)) (+ collected (count to-schedule))))))

(defn parse-forum-topic [db id type]
  (walk-forum-pages db {:id id :type type} 0 0))


