(ns bts.admin.scheduler
  (:use-macros [cljs.core.async.macros :only [go]])
  (:require [bts.admin.index :refer [layout]]
            [cljs.core.async :refer [<! timeout]]
            [clojure.string :as string]
            [cljs-time.format :as tf]
            [cljs-time.core :as t]
            [reagent.core :as r]
            [bts.components.input :refer [input]]
            [bts.admin.state :as state]
            [bts.admin.api :as api]))

(defn- status [s]
  (cond
    (= s "idle") [:i.icon-clock]
    (= s "work") [:i.icon-spin4.animate-spin]
    (= s "error") [:i.icon-attention]
    :else s))

(defn check-cron [s]
  (let [parts (string/split s #"\s")]
    (= (count parts) 7)))

(defn- format-time [s]
  (if (nil? s) ""
               (tf/unparse {:format-str "yyyy-MM-dd HH:mm:ss"}
                           (t/to-default-time-zone (tf/parse s)))))

(defn- table-row [_ idx item]
  (let [timer (atom nil)
        check-state (fn [] (go (let [name (:name @item)
                                     deleted (:_deleted @item)
                                     cs (if (or (nil? name) deleted)
                                          {:error "EMPTY"}
                                          (<! (api/schedule-info name)))]
                                 (when (nil? (:error cs))
                                   (state/schedule-item! idx cs)))))
        on-edit (fn [_] (do (state/scheduler-edit! @item)
                            (state/scheduler-show-edit! true)))
        on-delete (fn [] (go (let [r (<! (api/schedule-delete (:name @item)))]
                               (when (nil? (:error r))
                                 (state/schedule-delete! idx)))))]
    (r/create-class
      {:component-did-mount    (fn [] (reset! timer (js/setInterval #(check-state) 5000)))
       :component-will-unmount (fn [] (do (js/clearInterval @timer) (reset! timer nil)))
       :display-name           "schedule-table-row"
       :reagent-render         (fn []
                                 [:tr {:class (if (:_deleted @item) "hide" "")}
                                  [:td (:name @item)]
                                  [:td (:cron @item)]
                                  [:td.center (status (:status @item))]
                                  [:td (format-time (:last-start-at @item))]
                                  [:td (format-time (:last-complete-at @item))]
                                  [:td (:last-result @item)]
                                  [:td
                                   (if (= "work" (:status @item))
                                     [:button.standard
                                      {:on-click #(api/schedule-start (:name @item))}
                                      [:i.icon-cw-outline]]
                                     [:button.standard
                                      {:on-click #(api/schedule-start (:name @item))}
                                      [:i.icon-play-outline]])
                                   [:button.standard {:on-click on-edit} [:i.icon-pencil]]
                                   [:button.standard {:on-click on-delete} [:i.icon-trash-empty]]]])})))

(defn- table [items]
  [:table
   [:thead
    [:tr
     [:th "Name"]
     [:th "Cron"]
     [:th "Status"]
     [:th "Last Start"]
     [:th "Last Complete"]
     [:th "Last Result"]
     [:th]]]
   [:tbody
    (doall (for [[idx item] (map-indexed list @items)]
             [table-row {:key (:name item)} idx (state/scheduler-item idx)]))]])

(defn- new-job-pane [show]
  (let [empty-model {:name        ""
                     :action-name ""
                     :params      ""
                     :description ""
                     :cron        ""}
        model (r/atom empty-model)
        saving (r/atom false)
        on-submit (fn [] (do
                           (swap! model assoc :_submitted true)
                           (when (every? #(true? (get % 1)) (:_validate @model))
                             (go (reset! saving true)
                                 (let [result (<! (api/schedule-new (:name @model)
                                                                    (:action-name @model)
                                                                    (:params @model)
                                                                    (:description @model)
                                                                    (:cron @model)))]
                                   (reset! saving false)
                                   (when (nil? (:error result))
                                     (state/schedule-add! result)
                                     (reset! model empty-model)
                                     (reset! show false)))))))]
    (fn []
      [:div.slideover {:class (if @show "slideover-show" "slideover-hide")}
       [:h2 "Creating new job"]
       [:form.form {:on-submit #(do (.preventDefault %) (on-submit))}
        [:fieldset
         [:label
          [:span "Identificator"]
          [input {:placeholder "parser-rutracker-forum-313"
                  :model       model
                  :id          :name
                  :field       :text
                  :disabled    saving
                  :validator   :not-blank}]]
         [:label
          [:span "Action name"]
          [:p "Available actions: " [:br]
           [:strong "rutracker-forum-parse-film"]
           " - parse film type rutracker forum page for collect topics for store" [:br]
           [:strong "cleanup"]
           "- clean system temp data" [:br]
           [:strong "torrent-seed-leach-update"]
           "- update torrent seeder/leeder information"]
          [input {:placeholder "rutracker-forum-parse-film"
                  :model       model
                  :id          :action-name
                  :field       :text
                  :disabled    saving
                  :validator   :not-blank}]]
         [:label
          [:span "Parameters"]
          [:p
           [:strong "rutracker-forum-parse-film"] " - {:id \"123\"}" [:br]
           [:strong "torrent-seed-leach-update"] " - {}" [:br]]
          [input {:placeholder "{}"
                  :model       model
                  :id          :params
                  :field       :text
                  :disabled    saving
                  :validator   :not-blank}]]
         [:label
          [:span "Description"]
          [input {:model    model
                  :id       :description
                  :field    :text
                  :disabled saving}]]
         [:label
          [:span "Cron"]
          [:p "[second] [minute] [hour] [day] [month] [day of week] [year]"]
          [input {:model       model
                  :placeholder "0 0 */10 * * ? *"
                  :id          :cron
                  :field       :text
                  :disabled    saving
                  :validator   check-cron}]]]
        [:div.form__footer
         [:button.standard {:type "button" :disabled @saving :on-click #(reset! show false)} "cancel"]
         [:button.primary {:type "submit" :disabled @saving} (if @saving [:span [:i.icon-spin4.animate-spin] "saving"] "save")]]]])))

(defn- edit-job-pane [model show]
  (let [saving (r/atom false)
        on-submit (fn [] (do
                           (swap! model assoc :_submitted true)
                           (when (every? #(true? (get % 1)) (:_validate @model))
                             (go (reset! saving true)
                                 (let [result (<! (api/schedule-update (:name @model)
                                                                       (:action-name @model)
                                                                       (:params @model)
                                                                       (:description @model)
                                                                       (:cron @model)))]
                                   (reset! saving false)
                                   (when (nil? (:error result))
                                     (state/schedule-edit! result)
                                     (reset! model result)
                                     (reset! show false)))))))]
    (fn []
      [:div.slideover {:class (if @show "slideover-show" "slideover-hide")}
       [:h2 "Edit job settings"]
       [:form.form {:on-submit #(do (.preventDefault %) (on-submit))}
        [:fieldset
         [:label
          [:span "Identificator"]
          [input {:placeholder "parser-rutracker-forum-313"
                  :model       model
                  :id          :name
                  :field       :text
                  :disabled    true
                  :validator   :not-blank}]]
         [:label
          [:span "Action name"]
          [:p "Available actions: " [:br]
           [:strong "rutracker-forum-parse-film"]
           " - parse film type rutracker forum page for collect topics for store" [:br]
           [:strong "cleanup"]
           "- clean system temp data" [:br]
           [:strong "torrent-seed-leach-update"]
           "- update torrent seeder/leeder information"]
          [input {:placeholder "rutracker-forum-parse-film"
                  :model       model
                  :id          :action-name
                  :field       :text
                  :disabled    saving
                  :validator   :not-blank}]]
         [:label
          [:span "Parameters"]
          [:p
           [:strong "rutracker-forum-parse-film"] " - {:id \"123\"}" [:br]
           [:strong "torrent-seed-leach-update"] " - {}" [:br]]
          [input {:placeholder "{}"
                  :model       model
                  :id          :params
                  :field       :text
                  :disabled    saving
                  :validator   :not-blank}]]
         [:label
          [:span "Description"]
          [input {:model    model
                  :id       :description
                  :field    :text
                  :disabled saving}]]
         [:label
          [:span "Cron"]
          [:p "[second] [minute] [hour] [day] [month] [day of week] [year]"]
          [input {:model       model
                  :placeholder "0 0 */10 * * ? *"
                  :id          :cron
                  :field       :text
                  :disabled    saving
                  :validator   check-cron}]]]
        [:div.form__footer
         [:button.standard {:type "button" :disabled @saving :on-click #(reset! show false)} "cancel"]
         [:button.primary {:type "submit" :disabled @saving} (if @saving [:span [:i.icon-spin4.animate-spin] "saving"] "save")]]]])))

(defn component []
  (go (let [items (<! (api/schedule-list))]
        (when (nil? (:error items)) (state/schedule-list! items))))
  (fn []
    [layout
     [:h3 "List of schedulers"]
     [:div.pane
      [:button.primary {:type     "button"
                        :on-click #(state/scheduler-show-new! true)} "Create new job"]]
     [table (state/scheduler-list)]
     [new-job-pane (state/scheduler-show-new)]
     [edit-job-pane (state/scheduler-edit) (state/scheduler-show-edit)]]))