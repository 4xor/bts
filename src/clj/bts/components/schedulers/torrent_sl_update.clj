(ns bts.components.schedulers.torrent-sl-update
  (:require [bts.grab.rutracker :as gr]
            [hugsql.core :as hugsql]))

(hugsql/def-db-fns "sql/torrent.sql")

(defn update-sl-info [db]
  (let [for-seed-update (get-torrents-for-seed-update db)]
    (let [res (gr/seed-leech-info-pack for-seed-update)]
      (doseq [{id :id seeders :seeders leechers :leechers} res]
        (update-seed-leech-info db {:id       id
                                      :seeders  seeders
                                      :leechers leechers}))
      res)
    (if (empty? for-seed-update) (count for-seed-update) (+ (count for-seed-update) (update-sl-info db)))))