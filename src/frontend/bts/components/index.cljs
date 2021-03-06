(ns bts.components.index
  (:use-macros [cljs.core.async.macros :only [go]])
  (:require clojure.contrib.humanize
            [reagent.core :as r]
            [clojure.string :as string]
            [cljs.core.async :refer [<! timeout]]
            [cljs-http.client :as http]
            [bts.state :refer [state lang]]
            [bts.lang :refer [tr]]
            [bts.nav :as nav]))

(defn on-search [q]
  (swap! state assoc :result {:loading true})
  (swap! state assoc :current-query q)
  (let [uri (str "/?q=" q "&sort-by=" (get @state :sort) "&sort-dir=" (get @state :sort-dir))]
    (nav/goto uri))
  (go (let [response (<! (http/get "/search" {:query-params {"q" q "sort-by" (get @state :sort) "sort-dir" (get @state :sort-dir)}}))
            body (:body response)]
        (swap! state assoc :result {:data (:data body) :total (:total body) :loaded (count (:data body))}))))

(defn on-load-more []
  (swap! state assoc-in [:result :loading] true)
  (go (let [response (<! (http/get "/search" {:query-params {"q"        (get @state :current-query)
                                                             "sort-by"  (get @state :sort)
                                                             "sort-dir" (get @state :sort-dir)
                                                             "skip"     (get-in @state [:result :loaded])}}))
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

(defn search [q sort sort-dir]
  (let [sort-is-down (r/atom false)
        on-sort (fn [field]
                  (do (if (= @sort field)
                        (reset! sort-dir (if (= @sort-dir "asc") "desc" "asc"))
                        (do
                          (reset! sort field)
                          (reset! sort-dir "desc")))
                      (on-search @q)
                      (reset! sort-is-down false)))]

    (fn []
      [:form.search {:on-submit (fn [e] (.preventDefault e) (on-search @q))}
       [:div.field-set
        [:label (tr [(lang) :en] [:search/title])]
        [:input.q {:name          "q"
                   :type          "text"
                   :value         @q
                   :on-change     #(reset! q (. % -target.value))
                   :auto-complete "off"
                   :placeholder   (tr [(lang) :en] [:search/placeholder])
                   :on-focus      #(swap! state assoc-in [:help :type] :query)
                   :on-click      #(swap! state assoc-in [:help :type] :query)
                   :on-blur       #(swap! state assoc-in [:help :type] :none)}]
        [:button.primary {:type "submit"} [:i.icon.icon-search] (tr [(lang) :en] [:search/button])]
        [:div.sort
         [:button.standard {:type "button" :on-click #(swap! sort-is-down not)}
          (if (= @sort-dir "desc")
            [:i.icon.icon-sort-name-down]
            [:i.icon.icon-sort-name-up])
          (cond
            (= @sort "seed") (tr [(lang) :en] [:search/sort-seed])
            (= @sort "size") (tr [(lang) :en] [:search/sort-size])
            (= @sort "name") (tr [(lang) :en] [:search/sort-name]))
          (if @sort-is-down [:i.icon.icon-up-dir] [:i.icon.icon-down-dir])]
         [:div.sort-drop-down {:class (when @sort-is-down "show")}
          [:ul
           [:li {:class (when (= @sort "seed") "selected") :on-click #(on-sort "seed")} (tr [(lang) :en] [:search/sort-seed])]
           [:li {:class (when (= @sort "size") "selected") :on-click #(on-sort "size")} (tr [(lang) :en] [:search/sort-size])]
           [:li {:class (when (= @sort "name") "selected") :on-click #(on-sort "name")} (tr [(lang) :en] [:search/sort-name])]]]]]])))

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
    {:all  tags
     :kp   {:id (tag-value kpid) :value kp}
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
     [:div.loading [:i.icon-spin4.animate-spin] (tr [(lang) :en] [:search/loading])])
   (when-not (nil? (:data @items))
     [:div.search-footer
      (when-not (= (:loaded @items) (:total @items))
        [:button.standard.load-more {:on-click #(on-load-more)} (tr [(lang) :en] [:search/load-more])])
      [:div.load-count (:loaded @items) "/" (:total @items)]])])

(defn help-query []
  [:div.help
   [:h2.help__title (tr [(lang) :en] [:help/title])]
   (tr [(lang) :en] [:help/n1])
   (tr [(lang) :en] [:help/n2])
   (tr [(lang) :en] [:help/n3])
   (tr [(lang) :en] [:help/n4])
   (tr [(lang) :en] [:help/body])])

(defn help-torrent [item]
  [:div])

(defn component []
  (when-not (string/blank? (:q @state)) (on-search (:q @state)))
  (fn []
    [:div
     [:div.content
      [:div.aside
       [:h1 "[BTS] Torrent Search"]
       [search (r/cursor state [:q]) (r/cursor state [:sort]) (r/cursor state [:sort-dir])]
       [search-result (r/cursor state [:result])]]
      [:div.right-aside
       [:div.info
        (if (= (get-in @state [:help :type]) :query)
          [help-query]
          [help-torrent (r/cursor state [:help :value])])
        ]]]]))
