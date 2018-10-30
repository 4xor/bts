(ns bts.utils
  (:require [clojure.walk :as walk]
            [clojure.string :as str])
  (:import (java.io StringWriter PrintWriter)))

(defn round
  [d precision]
  (let [factor (Math/pow 10 precision)
        ret (/ (Math/floor (* d factor)) factor)]
    (if (= precision 0) (int ret) ret)
   ))

(defn decode-key
  "Converts a train case string into a snake case keyword."
  [s]
  (keyword (str/replace (name s) "_" "-")))

(defn encode-key
  "Converts a snake case keyword into a train case string."
  [k]
  (str/replace (name k) "-" "_"))

(defn transform-keys
  "Recursively transforms all map keys in coll with the transform-key fn."
  [transform-key coll]
  (letfn [(transform [x] (if (map? x)
                           (into {} (map (fn [[k v]] [(transform-key k) v]) x))
                           x))]
    (walk/postwalk transform coll)))

(defn json [body]
  {:status  200
   :headers {}
   :body    (transform-keys decode-key body)})


(defn exception->string [ex]
  (let [sw (StringWriter.)]
    (.write sw (.getMessage ex))
    (.write sw "\n")
    (.printStackTrace ex (PrintWriter. sw))
    (.toString sw)))
