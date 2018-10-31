(ns bts.components.schedulers.torrent-update
  (:require [hugsql.core :as hugsql]
            [bts.worker.rutracker :as rutracker]))

(hugsql/def-db-fns "sql/torrent.sql")

(defn update-torrent-info [db]
  (loop [updated 0]
    (let [for-update (get-torrents-for-update db)]
      (doseq [t for-update]
        (rutracker/schedule db (:id t) (cond
                                         (= (:torrent_type t) 1) :film
                                         (= (:torrent_type t) 2) :tv-series)))
      (if (empty? for-update)
        updated
        (recur (+ updated (count for-update)))))))