(ns kixi.eventlog.web-server
  (:require [compojure.core :refer [routes POST GET]]
            [compojure.route :refer [not-found]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [kixi.eventlog.api :refer [index-resource]]
            [kixi.event.topic :refer [publish]]
            [org.httpkit.server :as http-kit]
            [com.stuartsierra.component :as component]
            [clojure.tools.logging :as log]))

(defn all-routes [publish-fn]
  (routes
   (POST "/events" [] (index-resource publish-fn))
   (GET "/_elb_status" []  "ALL GOOD")
   (POST "/_elb_status" []  "ALL GOOD")
   (not-found {:headers {"Content-Type" "application/json"}
               :body "{\"error\": \"No Such Endpoint\"}"})))

(defrecord WebServer [opts]
  component/Lifecycle
  (start [this]
    (log/info "Starting Webserver")
    (let [server (http-kit/run-server (wrap-defaults
                                       (all-routes (partial publish (:topic this)))
                                       api-defaults) opts)]
      (assoc this ::server server)))
  (stop [this]
    (log/info "Stopping Webserver")
    (when-let [close-fn (::server this)]
      (close-fn))))

(defn new-server []
  (->WebServer {:verbose? true
                :port 8080
                :max-body
                (* 16 1024 1024)}))
