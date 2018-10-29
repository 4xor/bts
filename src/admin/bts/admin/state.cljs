(ns bts.admin.state
  (:require [reagent.core :as r]
            [clojure.data :refer [diff]]))

(defonce state (r/atom {:page      :index
                        :scheduler {:list      nil
                                    :loading   false
                                    :selected  nil
                                    :show-new  false
                                    :edit      nil
                                    :shod-edit false}
                        :dashboard {:task-stats nil
                                    :torrent-stats nil}}))

(defn- set-state [name f]
  (let [old @state]
    (f)
    (let [[in-new in-old both] (diff @state old)]
      (when in-new (js/console.log "[" name "]" (clj->js in-new))))))

(defn schedule-add! [item]
  (set-state "SCHEDULE-ADD" #(swap! state update-in [:scheduler :list] conj item)))

(defn schedule-delete! [idx]
  (set-state "SCHEDULE-DELETE" #(swap! state assoc-in [:scheduler :list idx :_deleted] true)))

(defn schedule-list! [items]
  (set-state "SCHEDULE-LIST" #(swap! state assoc-in [:scheduler :list] items)))

(defn schedule-item! [idx item]
  (set-state "SCHEDULE-ITEM" #(swap! state assoc-in [:scheduler :list idx] item)))

(defn schedule-edit! [item]
  (let [idx (first (keep-indexed #(if (= (:name %2) (:name item)) %1) (get-in @state [:scheduler :list])))]
    (schedule-item! idx item)))

(defn scheduler [] (r/cursor state [:scheduler]))
(defn scheduler-list [] (r/cursor state [:scheduler :list]))
(defn scheduler-item [idx] (r/cursor state [:scheduler :list idx]))
(defn scheduler-show-new [] (r/cursor state [:scheduler :show-new]))
(defn scheduler-show-new! [value] (set-state "SCHEDULE-SHOW-NEW" #(reset! (scheduler-show-new) value)))
(defn scheduler-show-edit [] (r/cursor state [:scheduler :show-edit]))
(defn scheduler-show-edit! [value] (set-state "SCHEDULE-SHOW-EDIT" #(reset! (scheduler-show-edit) value)))
(defn scheduler-edit [] (r/cursor state [:scheduler :edit]))
(defn scheduler-edit! [value] (set-state "SCHEDULE-EDIT" #(reset! (scheduler-edit) value)))

(defn dashboard-task-stats! [items]
  (set-state "DASHBOARD-TASK-STATS" #(swap! state assoc-in [:dashboard :task-stats] items)))
(defn dashboard-task-stats [] (r/cursor state [:dashboard :task-stats]))

(defn dashboard-torrent-stats! [items]
  (set-state "DASHBOARD-TORRENT-STATS" #(swap! state assoc-in [:dashboard :torrent-stats] items)))
(defn dashboard-torrent-stats [] (r/cursor state [:dashboard :torrent-stats]))

(defn page [] (r/cursor state [:page]))