(ns bts.grab.classif
  (:require [clojure.string :as str]))

(defn translate [source]
  (if (str/blank? source) nil
      (let [ls (str/lower-case source)]
        (cond
          (str/includes? ls "lostfilm") :lostfilm
          (str/includes? ls "jaskier") :jaskier
          (str/includes? ls "newstudio") :newstudio
          (str/includes? ls "amedia") :amedia
          (str/includes? ls "alexfilm") :alexfilm
          (str/includes? ls "кураж-бамбей") :kuraj-bombey
          (str/includes? ls "кубик в кубе") :k3
          (str/includes? ls "itunes") :itunes
          (str/includes? ls "профессиональный") :pro
          (str/includes? ls "дублированный") :pro
          (str/includes? ls "дублирован") :pro
          (str/includes? ls "дубляж") :pro
          (str/includes? ls "многоголосный") :multivoice
          (str/includes? ls "многоголосый") :multivoice
          (str/includes? ls "двухголосный") :twovoice
          (str/includes? ls "двухголосый") :twovoice
          (str/includes? ls "одноголосный") :onevoice
          (str/includes? ls "одноголосый") :onevoice
          (str/includes? ls "авторский") :author
          (str/includes? ls "любительский") :user
          (str/includes? ls "субтитры") :sub
          :else source))))

(defn video-quality [source]
  (if (str/blank? source) nil
      (let [ls (str/lower-case source)]
        (cond
          (or (str/includes? ls "bdrip")
              (str/includes? ls "bd-rip")
              (str/includes? ls "bdremix")
              (str/includes? ls "bdremux")
              (str/includes? ls "bd-remix")
              (str/includes? ls "bd-remux")
              (= ls "bd")
              (= ls "1080p")) :bdrip
          (or (str/includes? ls "blu") (str/includes? ls "bd-50")) :blu-ray
          (and (str/includes? ls "hd") (str/includes? ls "dvd")) :hd-dvd
          (and (str/includes? ls "hd") (str/includes? ls "tv")) :hd-tv
          (str/includes? ls "hd") :hd
          (str/includes? ls "720p") :hd
          (str/includes? ls "theater") :vhs
          (str/includes? ls "disc") :dvd
          (str/includes? ls "web") :web
          :else source))))
