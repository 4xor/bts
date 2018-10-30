(ns bts.web
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.coercions :refer :all]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-params]]
            [ring.util.response :as response]
            [clojure.tools.logging :as log]
            [environ.core :refer [env]]
            [ring.middleware.basic-authentication :refer [wrap-basic-authentication]]
            [clojure.string :as str]

            [bts.db :refer [db]]
            [bts.components.scheduler :as cmp-sch]
            [bts.components.dashboard :as cmp-dash]
            [bts.components.search :as cmp-search]))

(def ^:private admin-password (env :admin-password "dev"))

(defn authenticated? [username password]
  (println username password)
  (true? (and (= username "admin") (= password admin-password))))

(defn try-as-int [p default]
  (if (nil? p) default (try (Integer/parseInt p) (catch Exception _ default))))

(defroutes public-routes
           (route/resources "/" {:root "public"})
           (GET "/" [] (-> (response/resource-response "index.html" {:root "public"})
                           (response/content-type "text/html")))
           (GET "/search" [q skip] (response/response (cmp-search/search db q (try-as-int skip 0)))))

(defroutes admin-routes
           (GET "/admin" [] (-> (response/resource-response "admin.html" {:root "public"})
                                (response/content-type "text/html")))
           (context "/api/admin/v1/schedulers" [] (cmp-sch/handler db))
           (context "/api/admin/v1/dashboard" [] (cmp-dash/handler db)))

(defroutes app-routes
           public-routes
           admin-routes
           (route/not-found "Not Found"))

(defn- wrap-exception-handling
  [handler]
  (fn [request]
    (try
      (handler request)
      (catch Exception e (do (log/error e "Request exception" request)
                             {:status 500 :body {:error "INTERNAL_EXCEPTION"}})))))

(defn wrap-guard [app]
  (let [guard (wrap-basic-authentication app authenticated?)]
    (fn [req]
      (if (or (str/starts-with? (:uri req) "/admin") (str/starts-with? (:uri req) "/api/admin"))
        (guard req)
        (app req)))))

(def handler (-> #'app-routes
                 (wrap-exception-handling)
                 (wrap-guard)
                 (wrap-json-response {:keywords? true})
                 (wrap-json-params {:keywords? true})
                 (wrap-params (get-in site-defaults [:params :urlencoded] false))
                 (wrap-reload)))
