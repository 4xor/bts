(ns bts.state
  (:require [reagent.core :as r]))

(defn detect-lang []
  (let [ln (aget js/window "navigator" "language")]
    (cond
      (= ln "ru-RU") :ru
      :else :en)))

(defonce state (r/atom {:q ""
                        :result nil
                        :lang (try (detect-lang) (catch ExceptionInfo _ :en))
                        :help {:type :query}}))

(defn lang [] (get @state :lang))