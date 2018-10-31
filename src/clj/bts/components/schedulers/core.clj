(ns bts.components.schedulers.core
  (:require bts.components.schedulers.rutracker-parser
            bts.components.schedulers.torrent-sl-update
            bts.components.schedulers.torrent-update
            bts.components.schedulers.cleanup
            [clojure.tools.logging :as log]))

(defn execute-schedule-action [db action params]
  (try (cond
         (= action "rutracker-forum-parse-film") (bts.components.schedulers.rutracker-parser/parse-forum-topic db (:id params) :film)
         (= action "rutracker-forum-parse-tv-series") (bts.components.schedulers.rutracker-parser/parse-forum-topic db (:id params) :tv-series)
         (= action "torrent-seed-leach-update") (bts.components.schedulers.torrent-sl-update/update-sl-info db)
         (= action "torrent-update") (bts.components.schedulers.torrent-update/update-torrent-info db)
         (= action "cleanup") (bts.components.schedulers.cleanup/clean-up db)
         :else {:error "Action not found"})
       (catch Exception ex (do
                             (log/error ex "Action" action "with" params "failed")
                             {:error (.getMessage ex)}))))
