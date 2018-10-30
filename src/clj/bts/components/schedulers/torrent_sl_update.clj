(ns bts.components.schedulers.torrent-sl-update
  (:require [bts.grab.rutracker :as gr]
            [hugsql.core :as hugsql]))

(hugsql/def-db-fns "sql/torrent.sql")

(defn- safe-seed-leech-info-pack [list]
  (loop [retry 3]
    (let [res (try (gr/seed-leech-info-pack list) (catch Exception ex ex))]
      (if (instance? Exception res)
        (if (<= 0 retry)
          (throw (RuntimeException. "Failed to get information about seed/leed" res))
          (do (Thread/sleep 1000)
              (recur (dec retry))))
        res))))

(defn update-sl-info [db]
  (loop [updated 0]
    (let [for-seed-update (get-torrents-for-seed-update db)
          res (safe-seed-leech-info-pack for-seed-update)]
      (doseq [{id :id seeders :seeders leechers :leechers} res]
        (update-seed-leech-info db {:id       id
                                    :seeders  seeders
                                    :leechers leechers}))
      (if (empty? for-seed-update)
        updated
        (recur (+ updated (count for-seed-update)))))))