(ns bts.grab.rutracker
  (:require [bts.grab.parser :as parser]
            [clojure.string :as string]
            [cheshire.core :refer [parse-string]]
            [clj-http.client :as client]))

(defn- extract-title [body page-title]
  (let [[_ r] (re-find #"([^\(]+)(?:\s\(|::)" page-title)] r))

(defn- decode-title [title]
  (let [sp (string/split title #"/")
        sp-size (count sp)]
    (cond
      (= sp-size 2) {:ru (string/trim (get sp 0)) :original (string/trim (get sp 1))}
      :else {:original (string/trim title)})))

(defn- decode-translate [body]
  (last (re-find #"Перевод[^:]*:\s*([^:]*)\s[^:]*:" body)))

(defn- decode-video [body]
  (let [[_ w h] (re-find #"Видео:.*\s(\d{4}|\d{3}|\d{2})x(\d{4}|\d{3}|d{2})\s" body)
        [_ q] (re-find #"(?:Качество|Тип релиза)[^:]*:\s*([^\s]*)\s" body)]
    (if (and (nil? q) (nil? w) (nil? h))
      nil
      (merge-with into (if-not
                         (or (nil? w) (nil? h))
                         {:width (Integer. w) :height (Integer. h)}
                         {})
                  (if-not (nil? q) {:quality q} {})))))

(defn- decode-size [text]
  (if (nil? text)
    nil
    (let [[_ d s] (re-find #"(\d+(?:\.\d+)?)\s([G|M]B)" text)
          dn (Float/parseFloat d)]
      (cond
        (= s "GB") (* dn 1000000000)
        (= s "MB") (* dn 1000000)
        :else (* dn 1000)))))

(defn- decode-kp-link [l]
  (let [[_ a b] (re-find #"kinopoisk.*\/(?:film\/(\d+)|(\d+)\.gif)" l)] (or a b nil)))

(defn- decode-kp-id [body]
  (let [links (map #(get-in % [:attrs :href]) (parser/query body ".postLink"))
        images (map #(get-in % [:attrs :title]) (parser/query body "var"))
        kp-links (map #(decode-kp-link %) (into [] (concat links images)))]
    (first (filter #(not (nil? %)) kp-links))))

(defn topics [forum skip]
  (let [skip-uri (if (= 0 skip) "" (str "&start=" skip))
        uri (str "http://rutracker.org/forum/viewforum.php?f=" forum skip-uri)
        doc (parser/from-url uri)]
    (map #(get-in % [:attrs :data-topic_id]) (parser/query doc "*[data-topic_id]"))))

(defn topic [id]
  (let [doc (parser/from-url (str "http://rutracker.org/forum/viewtopic.php?t=" id))
        messages (parser/query doc ".row1 .message .post_wrap")
        title (parser/query doc "title")]
    {:body  (first messages)
     :title (:text (first title))}))

(defn seed-leech-info-pack [list]
  (let [url (str "http://api.rutracker.org/v1/get_peer_stats?by=topic_id&val=" (string/join "," (map #(:source_id %) list)))
        content (client/get url {:as :json-string-keys})
        js (get-in content [:body "result"])]
    (map (fn [i]
           (let [[seed leech _] (get js (str (:source_id i)))]
             (merge i {:seeders seed :leechers leech}))) list)))

(defn decode-topic [{topic :body page-title :title}]
  (if-not (nil? topic)
    (let [body (first (parser/query topic ".post_body"))
          links (parser/query body ".postLink")
          images (parser/query body "var.postImg")
          magnet (first (parser/query topic ".magnet-link"))
          size-el (first (parser/query topic ".attach_link.guest"))
          kp-id (decode-kp-id body)]
      {:title     (decode-title (extract-title topic page-title))
       :links     (map #(get-in % [:attrs :href]) links)
       :images    (map #(get-in % [:attrs :title]) images)
       :magnet    (get-in magnet [:attrs :href])
       :translate (decode-translate (:text body))
       :video     (decode-video (:text body))
       :size      (decode-size (:text size-el))
       :kp-id     kp-id})))
