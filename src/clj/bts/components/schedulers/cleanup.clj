(ns bts.components.schedulers.cleanup
  (:require [hugsql.core :as hugsql]))

(hugsql/def-db-fns "sql/queue.sql")

(defn clean-up [db]
  (delete-completed db))