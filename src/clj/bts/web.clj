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
            [bts.db :refer [db]]
            [bts.components.scheduler :as cmp-sch]
            [bts.components.dashboard :as cmp-dash]
            [bts.components.search :as cmp-search]))

(defn try-as-int [p default]
  (if (nil? p) default (try (Integer/parseInt p) (catch Exception _ default))))

(defroutes app-routes
           (route/resources "/" {:root "public"})
           (GET "/" [] (-> (response/resource-response "index.html" {:root "public"})
                           (response/content-type "text/html")))
           (GET "/admin" [] (-> (response/resource-response "admin.html" {:root "public"})
                                (response/content-type "text/html")))
           (GET "/search" [q skip] (response/response (cmp-search/search db q (try-as-int skip 0))))
           (context "/api/admin/v1/schedulers" [] (cmp-sch/handler db))
           (context "/api/admin/v1/dashboard" [] (cmp-dash/handler db))
           (route/not-found "Not Found"))

(defn- wrap-exception-handling
  [handler]
  (fn [request]
    (try
      (handler request)
      (catch Exception e (do (log/error e "Request exception" request)
                             {:status 500 :body {:error "INTERNAL_EXCEPTION"}})))))

(def handler (-> #'app-routes
                 (wrap-exception-handling)
                 (wrap-json-response {:keywords? true})
                 (wrap-json-params {:keywords? true})
                 (wrap-params (get-in site-defaults [:params :urlencoded] false))
                 (wrap-reload)))
