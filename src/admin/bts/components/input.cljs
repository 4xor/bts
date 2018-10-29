(ns bts.components.input
  (:require [clojure.string :as string]
            [reagent.core :as r]
            [cljs.core.async :refer [go]]))

(defn input [{:keys [model id field disabled validator placeholder validation-class]}]
  (let [vc (or validation-class "error")
        validator (cond
                    (fn? validator) validator
                    (= validator :not-blank) #(not (string/blank? %)))
        cls (atom [])
        on-change (fn [v] (do
                            (swap! model assoc id v)
                            (when validator (let [ret (validator v)]
                                              (swap! model assoc-in [:_validate id] ret)
                                              (if ret (reset! cls []) (reset! cls [vc]))))))]
    (fn []
      [:input {:class       (if (:_submitted @model) (string/join " " @cls) "")
               :disabled    (if (satisfies? IAtom disabled) @disabled disabled)
               :value       (get @model id)
               :placeholder placeholder
               :on-change   #(on-change (. % -target.value))}])))
