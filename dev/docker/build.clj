(ns docker.build
  (:require [clojure.string :as str]
            [clojure.java.shell :as shell])
  (:gen-class))

(def docker-username (System/getenv "DOCKER_USERNAME"))
(def docker-password (System/getenv "DOCKER_PASSWORD"))

(defn exec [cmd & {:keys [in] :or {in ""}}]
  (println "Exec" cmd)
  (let [{o :out e :err c :exit} (shell/sh "sh" "-c" cmd :in in)]
    (println o)
    (binding [*out* *err*] (println e))
    (println "Exit with code:" c)
    (when (not= c 0) (System/exit c))))

(defn -main [image-name version]
  (let [versions (if
                   (str/includes? version "SNAPSHOT")
                   ["nightly" version]
                   (into ["latest"]
                         (let [prts (str/split version #"\.")]
                           (map #(str/join "." (take % prts)) (range 1 (inc (count prts)))))))]
    (println "Versions" versions)
    (exec (str "docker login -u \"" docker-username "\" --password-stdin") :in docker-password)
    (exec (str "docker build " (str/join " " (map #(str "-t " image-name ":" %) versions)) " ."))
    (doseq [v versions]
      (exec (str "docker push " image-name ":" v)))
    (System/exit 0)))