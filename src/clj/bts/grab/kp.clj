(ns bts.grab.kp
  (:require [clojure.xml :as xml]
            [clojure.zip :as zip]
            [clojure.data.zip.xml :as zip-xml]
            [clojure.tools.logging :as log]))

(defn get-raiting [id]
  (try (let [content (slurp (str "https://rating.kinopoisk.ru/" id ".xml"))]
         (try (let [root (zip/xml-zip (xml/parse (java.io.ByteArrayInputStream. (.getBytes content))))]
                 {:kp (Float/parseFloat (zip-xml/xml1-> root :rating :kp_rating zip-xml/text))
                  :imdb (try (Float/parseFloat (zip-xml/xml1-> root :rating :imdb_rating zip-xml/text)) (catch Exception _ nil))})
              (catch Exception ex (log/error "Can not parse raiting" id "content" content) nil)))
       (catch Exception ex (log/error "Can not get raiting" id) nil)))
