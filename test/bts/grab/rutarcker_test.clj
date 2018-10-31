(ns bts.grab.rutarcker-test
  (:require [clojure.test :refer :all]
            [bts.grab.rutracker :refer :all]))

(deftest extract-title-test
  (is (= (extract-title "Скорая помощь (ER) | Сезон 1-14 | Серия 1-309 [1994-2007, США, Мелодрама / Драма / Комедия, WEB-DLRip] [НТВ]")
         "Скорая помощь"))
  (is (= (extract-title "Драма / Скорая помощь (ER) | Сезон 1-14 | Серия 1-309 [1994-2007, США, Мелодрама")
         "Драма / Скорая помощь"))
  (is (= (extract-title "Первому игроку приготовиться / Ready Player One (Стивен Спилберг / Steven Spielberg) [2018, США, фантастика, боевик, приключения, UHD BDRemux 2160p] Dub + Original (Eng) + Sub (Rus, Eng)")
         "Первому игроку приготовиться / Ready Player One"))
  (is (= (extract-title "Келин / Невестка / Kelin (Ермек Турсунов / Ermek Tursunov) [2009, Казахстан, Драма, BDRip 720p]")
         "Келин / Невестка / Kelin"))
  (is (= (extract-title "Экипаж (Николай Лебедев) [2016, Россия, драма, приключения, триллер, BDRip 720p]")
         "Экипаж")))

(deftest decode-tv-series-info-from-title-test
  (is (= (decode-tv-series-info-from-title "Ходячие мертвецы / The Walking Dead / Сезон: 9 / Серии: 1-4 из 16 (Грег Никотеро, Майкл Е. Сатраземис, Эрнест Р. Дикерсон) [2018, США, Ужасы, фантастика, триллер, драма, WEB-DLRip] MVO (FOX) + Original")
         {:title {:ru "Ходячие мертвецы" :original "The Walking Dead"}
          :season {:from 9 :to 9}
          :series {:from 1 :to 4}}))
  (is (= (decode-tv-series-info-from-title "Скорая помощь (ER) | Сезон 1-14 | Серия 1-309 [1994-2007, США, Мелодрама / Драма / Комедия, WEB-DLRip] [НТВ]")
         {:title {:original "Скорая помощь (ER)"}
          :season {:from 1 :to 14}
          :series {:from 1 :to 309}}))
  (is (= (decode-tv-series-info-from-title "Ворона / Серии: 1-5 из 12 (Ольга Ангелова, Евгений Сосницкий) [2018, детектив, WEBRip]")
         {:title {:original "Ворона"}
          :season nil
          :series {:from 1 :to 5}})))