(ns bts.worker.rutracker
  (:require [bts.worker.core :as w]
            [bts.grab.rutracker :as gr]
            [clojure.tools.logging :as log]
            [bts.grab.classif :as tc]
            [bts.grab.kp :as kp]
            [bts.utils :as utils]
            [clojure.string :as string]
            [hugsql.core :as hugsql]))

(hugsql/def-db-fns "sql/torrent.sql")
(def TASK_TYPE "rutracker-topic")

(defn- topic-name [{o :original r :ru}]
  (cond
    (nil? r) o
    :else (str o " / " r)))

(defn- tag-translate [t acc]
  (if-not (nil? t) (conj acc (cond
                               (= t :pro) "pro-voice"
                               (= t :itunes) "itunes-voice"
                               (= t :multivoice) "multi-voice"
                               (= t :twovoice) "two-voice"
                               (= t :onevoice) "one-voice"
                               (= t :author) "author-voice"
                               (= t :user) "user-voice"
                               (= t :sub) "sub"
                               :else (str "translate:" t))) acc))

(defn- tag-video-quality [q acc]
  (if-not (nil? q) (conj acc (cond
                               (= q :bdrip) "BDRip"
                               (= q :blu-ray) "Blu-Ray"
                               (= q :hd-dvd) "HD-DVD"
                               (= q :hd-tv) "HD-TV"
                               (= q :hd) "HD"
                               (= q :vhs) "VHS"
                               (= q :dvd) "DVD"
                               (= q :web) "WEB-DL"
                               :else (str "quality:" q))) acc))

(defn- tag-video-size [{{w :width h :height} :video} acc]
  (if-not (or (nil? w) (nil? h) (= w 0) (= h 0))
    (conj acc (str w "x" h)
          (cond
            (and (>= w 7600) (>= h 4000)) "4320p"
            (and (>= w 3900) (>= h 2000)) "2160p"
            (>= w 1900) "1080p"
            (>= w 1200) "720p"
            (>= w 700) "576p"
            :else "480p"))
    acc))

(defn- tag-kp [{id :kp-id} acc]
  (if-not (nil? id) (conj acc (str "kpid:" id)) acc))

(defn- tag-raiting-kp [{k :kp} acc]
  (if-not (nil? k)
    (let [values (map #(string/replace (str %) #"\.0$" "") [k (utils/round k 0) (utils/round k 1)])
          keys (map #(str "kp:" %) values)]
      (concat acc (distinct keys)))
    acc))

(defn- tag-raiting-imdb [{k :imdb} acc]
  (if-not (nil? k)
    (let [values (map #(string/replace (str %) #"\.0$" "") [k (utils/round k 0) (utils/round k 1)])
          keys (map #(str "imdb:" %) values)]
      (concat acc (distinct keys)))
    acc))

(defn- parse-video-topic [id]
  (let [t (gr/decode-topic (gr/topic id))
        name (topic-name (:title t))
        raiting (if-not (nil? (:kp-id t)) (kp/get-raiting (:kp-id t)) nil)
        tags (->> []
                  (tag-translate (tc/translate (:translate t)))
                  (tag-video-quality (tc/video-quality (get-in t [:video :quality])))
                  (tag-video-size t)
                  (tag-kp t)
                  (tag-raiting-kp raiting)
                  (tag-raiting-imdb raiting))]
    {:source       "rutracker"
     :source_id    id
     :torrent_type 1
     :magnet       (:magnet t)
     :size         (:size t)
     :name         name
     :tags         (into [] tags)}))

(defn- task-topic-parse-video [db id]
  (let [to-store (parse-video-topic id)]
    (if-not (nil? (:magnet to-store))
      (torrent-insert db to-store)
      (register-failed-topic db {:source    "rutracker"
                                 :source_id id
                                 :error     "NOT_TORRENT"}))))

(defn- task-topic-parse [db]
  (fn [_ {id :id torrent-type :type}]
    (try (cond
           (= torrent-type "film") (task-topic-parse-video db id)
           :else (throw (Exception. "unsupported torrent type")))
         (catch Throwable ex
           (register-failed-topic db {:source    "rutracker"
                                      :source_id id
                                      :error     (utils/exception->string ex)})
           (log/error ex "Parse topic exception " id) {:error (.getMessage ex)}))))

(defn start-topic-worker
  "Start worker witch will parse topics torrent data"
  [db]
  (w/start db TASK_TYPE (task-topic-parse db)))

(defn schedule [db id type]
  (w/schedule db TASK_TYPE {:id id :type type}))
