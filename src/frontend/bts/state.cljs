(ns bts.state
  (:require [reagent.core :as r]))

(defonce state (r/atom {:q ""
                        :result nil}))
