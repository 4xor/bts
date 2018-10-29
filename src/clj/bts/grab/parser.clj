(ns bts.grab.parser
  (:import org.jsoup.Jsoup))

(defn- parse-attributes [attrs]
  (into {} (map (fn [a]
                  (let [key (.getKey a)
                        value (.getValue a)]
                    (assoc {} (keyword key) value))) attrs)))

(defn- parse-elements [nodes]
  (into [] (map (fn [e] {:text  (.text e)
                         :attrs (parse-attributes (.attributes e))
                         :child (parse-elements (.children e))
                         :e     e})
                nodes)))

(defn from-url [url]
  (try {:e (.get (Jsoup/connect url))}
       (catch Exception ex (throw (Exception. (str "Failed to fetch " url) ex)))))

(defn parse [html]
  {:e (Jsoup/parse html)})

(defn query [{doc :e} q]
  (parse-elements (.select doc q)))



