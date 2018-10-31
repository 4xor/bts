(ns bts.lang
  (:require [taoensso.tempura :as tempura]
            [bts.state :refer [lang]]))

(def dictionary
  {
   :en
   {:search {:button      "search"
             :title       "query"
             :loading     "searching..."
             :placeholder "title +1080p +kp:8 +pro-voice"
             :load-more   "Load More"
             :sort-seed   "seeders"
             :sort-size   "size"
             :sort-name   "name"}
    :help   {:title "Help Query"
             :n1    [:p [:strong "t1 +1080p"] " - search with title \"t1\" and with tag \"1080p\""]
             :n2    [:p [:strong "t1 t2 -1080p"] " - search with title \"t1\" and \"t2\" and without tag \"1080p\""]
             :n3    [:p [:strong "+1080p +imdb:8"] " - search with \"1080p\" video quality and imdb rating >= 8 < 9"]
             :n4    [:p [:strong "Breaking Bad +s2 +1080p"] " - search \"Breaking Bad\" season num 2 in \"1080p\" video quality"]
             :body  [:p
                     [:br]
                     [:strong "Tags:"] [:br]
                     [:strong "4320p / 8k / 2160p / 4k / 1080p / 720p / 576p / 480p"] " - video quality" [:br]
                     [:strong "BDRip / Blue-Ray / HD-DVD / HD_TV / HD / VHS / DVD / WEB-DL"] " - video source" [:br]
                     [:strong "imdb:5 / imdb:5.3 / kp:8 / kp:8.3"] " - rating filter" [:br]
                     [:strong "s1 / s12"] " - season of tv-series" [:br]
                     [:strong "x1 / x12"] " - series num of tv-series" [:br]
                     [:br]
                     [:strong "Translate:"] [:br]
                     [:strong "lostfilm"] " - " [:a {:href "https://www.lostfilm.tv/" :target "_blank"} "LostFilm"] [:br]
                     [:strong "jaskier"] " - " [:a {:href "http://jaskier.ru/" :target "_blank"} "JASKIER"] [:br]
                     [:strong "newstudio"] " - " [:a {:href "http://newstudio.tv/" :target "_blank"} "NewStudio"] [:br]
                     [:strong "amedia"] " - " [:a {:href "http://www.amedia.ru/" :target "_blank"} "Amedia"] [:br]
                     [:strong "alexfilm"] " - " [:a {:href "https://alexfilm.cc/" :target "_blank"} "AlexFilm"] [:br]
                     [:strong "kuraj-bambey"] " - " [:a {:href "https://kuraj-bambey.ru/" :target "_blank"} "Kuraj Bambey"] [:br]
                     [:strong "k3"] " - " [:a {:href "http://kubik3.ru/" :target "_blank"} "Kubik3"] [:br]
                     [:strong "pro-voice"] " - Professional" [:br]
                     [:strong "itunes-voice"] " - Professional from iTunes" [:br]
                     [:strong "multi-voice"] " - Multi voice" [:br]
                     [:strong "two-voice"] " - Two voice" [:br]
                     [:strong "one-voice"] " - One voice" [:br]
                     [:strong "author-voice"] " - Author's translation" [:br]
                     [:strong "user-voice"] " - Amateur translation" [:br]
                     [:strong "sub"] " - Subtitles only"]}}
   :ru
   {:search {:button      "найти"
             :title       "поиск"
             :loading     "загрузка..."
             :placeholder "название +1080p +kp:8 +pro-voice"
             :load-more   "Показать Еще"
             :sort-seed   "сиды"
             :sort-size   "размер"
             :sort-name   "название"}
    :help   {:title "Помощь"
             :n1    [:p [:strong "t1 +1080p"] " - искать с заголовком \"t1\" и тегом \"1080p\""]
             :n2    [:p [:strong "t1 t2 -1080p"] " - искать с заголовком  \"t1\" и \"t2\" без тега \"1080p\""]
             :n3    [:p [:strong "+1080p +kp:8"] " - искать в \"1080p\" качестве видео и рейтингом Кинопоиска от 8 до 9"]
             :n4    [:p [:strong "Breaking Bad +s2 +1080p"] " - искать \"Breaking Bad\" 2 сезон в \"1080p\" качестве видео"]
             :body  [:p
                     [:br]
                     [:strong "Теги:"] [:br]
                     [:strong "4320p / 8k / 2160p / 4k / 1080p / 720p / 576p / 480p"] " - качество видео" [:br]
                     [:strong "BDRip / Blue-Ray / HD-DVD / HD_TV / HD / VHS / DVD / WEB-DL"] " - качество источника" [:br]
                     [:strong "imdb:5 / imdb:5.3 / kp:8 / kp:8.3"] " - фильтр по рейтингу" [:br]
                     [:strong "s1 / s12"] " - номер сезона для сериала" [:br]
                     [:strong "x1 / x12"] " - номер серсии для сериала" [:br]
                     [:br]
                     [:strong "Перевод и озвучка:"] [:br]
                     [:strong "lostfilm"] " - " [:a {:href "https://www.lostfilm.tv/" :target "_blank"} "LostFilm"] [:br]
                     [:strong "jaskier"] " - " [:a {:href "http://jaskier.ru/" :target "_blank"} "JASKIER"] [:br]
                     [:strong "newstudio"] " - " [:a {:href "http://newstudio.tv/" :target "_blank"} "NewStudio"] [:br]
                     [:strong "amedia"] " - " [:a {:href "http://www.amedia.ru/" :target "_blank"} "Amedia"] [:br]
                     [:strong "alexfilm"] " - " [:a {:href "https://alexfilm.cc/" :target "_blank"} "AlexFilm"] [:br]
                     [:strong "kuraj-bambey"] " - " [:a {:href "https://kuraj-bambey.ru/" :target "_blank"} "Kuraj Bambey"] [:br]
                     [:strong "k3"] " - " [:a {:href "http://kubik3.ru/" :target "_blank"} "Kubik3"] [:br]
                     [:strong "pro-voice"] " - Профессиональный" [:br]
                     [:strong "itunes-voice"] " - Профессиональный iTunes" [:br]
                     [:strong "multi-voice"] " - Многоголосый закадровый" [:br]
                     [:strong "two-voice"] " - Двуголосый" [:br]
                     [:strong "one-voice"] " - Одноголосый" [:br]
                     [:strong "author-voice"] " - Авторский перевод" [:br]
                     [:strong "user-voice"] " - Любительский перевод" [:br]
                     [:strong "sub"] " - Только субтитры"]}}})

(def tr (partial tempura/tr {:dict dictionary}))