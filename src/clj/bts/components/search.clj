(ns bts.components.search
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.string :as string]
            [clojure.core.reducers :as r]
            [clojure.tools.logging :as log]))

(defn parse-q [q]
  (let [m (re-seq #"(?:(-|\+)*\"([^\"]*)\"|(-|\+)*([^\s]*))" q)]
    (map (fn [[_ g1 g2 g3 g4]]
           (cond
             (and (not (string/blank? g3)) (not (string/blank? g4))) {:t :tag :v g4 :d (if (= g3 "+") :add :rem)}
             (and (not (string/blank? g1)) (not (string/blank? g2))) {:t :tag :v g2 :d (if (= g1 "+") :add :rem)}
             (not (string/blank? g4)) {:t :name :v g4 :d :add}
             (not (string/blank? g2)) {:t :name :v g2 :d :add}
             :else nil)) m)))

(defn sqlparams [parsed-q]
  (let [m (filter #(not (nil? %)) parsed-q)]
    (r/reduce (fn [{sql :sql params :params} {t :t v :v d :d}]
                (cond
                  (= t :name) {:sql (conj sql (str (if (= d :rem) " NOT (" "(") " lower(name) like ?)"))
                               :params (conj params (str "%" (string/lower-case v) "%"))}
                  (= t :tag) {:sql (conj sql (str (if (= d :rem) " NOT (" "(") " tags @> ARRAY[?::text])"))
                              :params (conj params (str v))}
                  :else {:sql sql :params params}
                  )
                ) {:sql [] :params []} m)
    ))

(defn sqlparams->sql [{sql :sql params :params} {skip :skip limit :limit order :order-by}]
  (let [wheres (string/join " AND " sql)
        where (if (string/blank? wheres) "" (str "where" wheres))
        s (str "select * from torrent " where " ORDER BY " order " LIMIT " limit " OFFSET " skip)
        sc (str "select count(*) as c from torrent " where)]
    {:list (into [s] params) :count (into [sc] params)}))

(defn escape-sort [sort-by sort-dir]
  (cond
    (and (= sort-by "seed") (= sort-dir "asc")) "seeders asc NULLS LAST"
    (and (= sort-by "seed") (= sort-dir "desc")) "seeders desc NULLS LAST"
    (and (= sort-by "size") (= sort-dir "asc")) "size asc"
    (and (= sort-by "size") (= sort-dir "desc")) "size desc"
    (and (= sort-by "name") (= sort-dir "asc")) "name asc"
    (and (= sort-by "size") (= sort-dir "desc")) "name desc"
    :else "seeders desc NULLS LAST"))

(defn search [db q skip sort-by sort-dir]
  (let [{sql :list count-sql :count} (-> q (parse-q) (sqlparams) (sqlparams->sql {:skip skip :limit 50 :order-by (str (escape-sort sort-by sort-dir) ",id asc")}))]
    (try
      {:data (into [] (jdbc/query db sql)) :total (jdbc/query db count-sql {:row-fn :c :result-set-fn first})}
      (catch Exception ex (log/error ex "Search SQL exception: " sql) (throw ex)))))

