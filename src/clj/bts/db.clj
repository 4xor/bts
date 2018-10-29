(ns bts.db
  (:require [jdbc.pool.c3p0 :as pool]
            [environ.core :refer [env]]
            [ragtime.jdbc :as migrate-jdbc]
            [ragtime.core :as migrate]
            clojure.java.jdbc))

(Class/forName "org.postgresql.Driver")

(def db
  (pool/make-datasource-spec
   {:connection-uri
    (let [host (env :rds-hostname nil)
          port (env :rds-port nil)
          db-name (env :rds-db-name nil)
          username (env :rds-username nil)
          password (env :rds-password nil)
          uri (env :pg-uri nil)]
      (if (or (nil? host) (nil? port) (nil? db-name) (nil? username) (nil? password))
        (or uri "jdbc:postgresql://127.0.0.1:5432/postgres?user=postgres")
        (str "jdbc:postgresql://" host ":" port "/" db-name "?user=" username "&password=" password)))}))

(defn migrate []
  "Execute migration up for main db"
  (let [db (migrate-jdbc/sql-database db)
        m (migrate-jdbc/load-resources "migrations")
        idx (migrate/into-index m)]
    (migrate/migrate-all db idx m)))

(extend-protocol clojure.java.jdbc/ISQLParameter
  clojure.lang.IPersistentVector
  (set-parameter [v ^java.sql.PreparedStatement stmt ^long i]
    (let [conn (.getConnection stmt)
          meta (.getParameterMetaData stmt)
          type-name (.getParameterTypeName meta i)]
      (if-let [elem-type (when (= (first type-name) \_) (apply str (rest type-name)))]
        (.setObject stmt i (.createArrayOf conn elem-type (to-array v)))
        (.setObject stmt i v)))))

(extend-protocol clojure.java.jdbc/IResultSetReadColumn
  java.sql.Array
  (result-set-read-column [val _ _]
    (into [] (.getArray val))))
