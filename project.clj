(defproject bts "0.1.0-SNAPSHOT"
  :description "BitTorrent parse and search with tags and metadata"
  :url "http://bts.4xor.io"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.jsoup/jsoup "1.11.3"]
                 [org.clojure/data.zip "0.1.2"]

                 [com.layerware/hugsql "0.4.9"]
                 [org.clojure/java.jdbc "0.7.8"]
                 [org.postgresql/postgresql "42.2.2"]
                 [clojure.jdbc/clojure.jdbc-c3p0 "0.3.3"]
                 [org.clojure/tools.logging "0.4.1"]
                 [environ "1.1.0"]
                 [ragtime "0.7.2"]
                 [cheshire "5.8.1"]
                 [org.slf4j/slf4j-api "1.7.25"]
                 [ch.qos.logback/logback-classic "1.2.3"]
                 [com.fasterxml.jackson.core/jackson-databind "2.9.7"]

                 [ring "1.7.0"]
                 [ring-basic-authentication "1.0.5"]
                 [aleph "0.4.6"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-json "0.4.0"]
                 [compojure "1.6.1"]
                 [clj-http "3.9.1"]
                 [clojurewerkz/quartzite "2.1.0" :exclusions [org.quartz-scheduler/quartz]]
                 [org.quartz-scheduler/quartz "2.3.0"]

                 [org.clojure/clojurescript "1.10.339"]
                 [org.clojure/core.async "0.4.474"]
                 [antizer "0.3.1"]
                 [reagent "0.8.1"]
                 [secretary "1.2.3"]
                 [cljs-http "0.1.45"]
                 [com.andrewmcveigh/cljs-time "0.5.2"]
                 [clojure-humanize "0.2.2"]]
  :plugins [[lein-ring "0.12.4"]
            [lein-figwheel "0.5.16"]
            [lein-cljsbuild "1.1.7"]]
  :java-source-paths ["src/java"]

  :ring {:handler bts.web/handler :auto-reload? true}
  :main ^:skip-aot bts.core
  :uberjar-name "bts.jar"
  :uberwar-name "bts.war"

  :source-paths ["src/clj"]
  :resource-paths ["resources"]
  :profiles {:uberjar {:source-paths ["src/clj"]
                       :prep-tasks   ["clean"
                                      "compile"
                                      ["cljsbuild" "once" "min"]
                                      ["cljsbuild" "once" "admin-min"]]
                       :hooks        []
                       :omit-source  true
                       :aot          :all}
             :dev     {:source-paths ["dev"]
                       :dependencies [[figwheel-sidecar "0.5.16"]
                                      [com.cemerick/piggieback "0.2.1"]]
                       :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]}}}
  :clean-targets ^{:protect false} [:target-path "out" "resources/public/bts" "resources/public/admin"]
  :cljsbuild {:builds [{:id           "dev"
                        :source-paths ["src/frontend"]
                        :figwheel     {:on-jsload "bts.core/on-js-reload"}
                        :compiler     {:main       bts.core
                                       :asset-path "bts/out"
                                       :output-to  "resources/public/bts/main.js"
                                       :output-dir "resources/public/bts/out"
                                       :npm-deps   false}}
                       {:id           "admin-dev"
                        :source-paths ["src/admin"]
                        :figwheel     {:on-jsload "bts.admin.core/on-js-reload"}
                        :compiler     {:main       bts.admin.core
                                       :asset-path "admin/out"
                                       :output-to  "resources/public/admin/main.js"
                                       :output-dir "resources/public/admin/out"
                                       :npm-deps   false}}
                       {:id           "min"
                        :source-paths ["src/frontend"]
                        :jar          true
                        :compiler     {:main                 bts.core
                                       :output-to            "resources/public/bts/main.js"
                                       :output-dir           "target/btsjs"
                                       :source-map-timestamp true
                                       :optimizations        :advanced
                                       :closure-defines      {goog.DEBUG false}
                                       :pretty-print         false}}
                       {:id           "admin-min"
                        :source-paths ["src/admin"]
                        :jar          true
                        :compiler     {:main                 bts.admin.core
                                       :output-to            "resources/public/admin/main.js"
                                       :output-dir           "target/adminjs"
                                       :source-map-timestamp true
                                       :optimizations        :advanced
                                       :closure-defines      {goog.DEBUG false}
                                       :pretty-print         false}}]}
  :figwheel {:css-dirs     ["resources/public/css"]
             :nrepl-port   7888
             :nrepl-host   "localhost"
             :ring-handler bts.web/handler}
  :aliases {"docker-publish" ["run" "-m" "docker.core" "4xor/bts" :project/version]})
