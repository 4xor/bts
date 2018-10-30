(ns bts.components.index
  (:use-macros [cljs.core.async.macros :only [go]])
  (:require clojure.contrib.humanize
            [reagent.core :as r]
            [clojure.string :as string]
            [cljs.core.async :refer [<! timeout]]
            [cljs-http.client :as http]
            [bts.state :refer [state]]))

(defn on-search [q]
  (swap! state assoc :result {:loading true})
  (swap! state assoc :current-query q)
  (go (let [response (<! (http/get "/search" {:query-params {"q" q}}))
            body (:body response)]
        (swap! state assoc :result {:data (:data body) :total (:total body) :loaded (count (:data body))}))))

(defn on-load-more []
  (swap! state assoc-in [:result :loading] true)
  (go (let [response (<! (http/get "/search" {:query-params {"q" (:current-query @state)
                                                             "skip" (get-in @state [:result :loaded])}}))
            body (:body response)]
        (swap! state assoc-in [:result :loading] false)
        (swap! state update-in [:result :data] into (:data body))
        (swap! state update-in [:result :loaded] + (count (:data body))))))

(defn on-tag-click [tag]
  (let [q (string/trim (:q @state))
        q2 (cond
             (string/includes? q (str "+" tag)) (string/replace q (str "+" tag) (str "-" tag))
             (string/includes? q (str "-" tag)) (string/replace q (str "-" tag) (str "+" tag))
             :else (str (if (string/blank? q) q (str q " ")) "+" tag)
             )]
    (swap! state assoc :q q2)
    (on-search q2)))

(defn search [q]
  [:form.search {:on-submit (fn [e] (.preventDefault e) (on-search @q))}
   [:div.field-set
    [:label "query"]
    [:input.q {:name "q" :type "text" :value @q :on-change #(reset! q (. % -target.value)) :auto-complete "off" :placeholder "title +1080p +kp:8 +pro-voice"}]
    [:button.primary {:type "submit"} [:i.icon.icon-search] "search"]]])

(defn tag-value [tag]
  (let [s (string/split tag ":")]
    (get s 1)))

(defn tags-parse [tags]
  (let [kpid (first (filter #(string/starts-with? % "kpid:") tags))
        kp (first (filter #(string/starts-with? % "kp:") tags))
        view (filter (fn [e] (not (or (string/starts-with? e "kpid:")
                                      (string/starts-with? e "kp:")
                                      (string/starts-with? e "imdb:")
                                      (re-matches #"s\d+" e)
                                      (re-matches #"x\d+" e)))) tags)]
    {:all tags
     :kp {:id (tag-value kpid) :value kp}
     :view view}))

(defn search-item [item]
  (let [{tags :view kp :kp} (tags-parse (:tags item))]
    [:div.search-item {:key (:id item)}
     [:div.name
      [:span.name (:name item)]
      [:a.link {:href (:magnet item)} [:span.sl "[S:" (:seeders item) " L:" (:leechers item) "] "] [:span.size (clojure.contrib.humanize/filesize (:size item))] [:i.icon.icon-magnet]]]
     [:div.tags
      (for [tag tags]
        [:div.tag {:key tag :on-click #(on-tag-click tag)} (str "#" tag)])
      (when-not (string/blank? (:id kp)) [:div.tag [:a.external {:href (str "https://www.kinopoisk.ru/film/" (:id kp)) :target "_blank"} (:value kp) [:i.icon.icon-link-ext]]])]]))

(defn search-result [items]
  [:div.search-result
   (for [item (:data @items)]
     (search-item item))
   (if (:loading @items)
     [:div.loading [:i.icon-spin4.animate-spin] "searching..."])
   (when-not (nil? (:data @items))
     [:div.search-footer
      (when-not (= (:loaded @items) (:total @items))
        [:button.standard.load-more {:on-click #(on-load-more)} "Load More"])
      [:div.load-count (:loaded @items) "/" (:total @items)]])])

(defn component []
  [:div
   [:h1 "[BTS] Torrent Search"]
   [search (r/cursor state [:q])]
   [search-result (r/cursor state [:result])]])
