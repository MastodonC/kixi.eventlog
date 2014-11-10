(ns kixi.eventlog.web-server
  (:require [compojure.core :refer [routes POST GET]]
            [compojure.route :refer [not-found]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [kixi.eventlog.api :refer [index-resource]]
            [org.httpkit.server :as http-kit]
            [com.stuartsierra.component :as component]
            [kixi.event.producer :refer [publish]]
            ))

(defn all-routes [topic]
  (routes
   (POST "/events" [] (index-resource (partial publish topic)))
   (GET "/_elb_status" []  "ALL GOOD")
   (POST "/_elb_status" []  "ALL GOOD")
   (not-found {:headers {"Content-Type" "application/json"}
               :body "{\"error\": \"No Such Endpoint\"}"})))

(defrecord WebServer [opts]
  component/Lifecycle
  (start [this]
    (println "Starting Webserver")
    (let [server (http-kit/run-server (wrap-defaults
                                       (all-routes (:topic this))
                                       api-defaults) opts)]
      (assoc this ::server server)))
  (stop [this]
    (println "Stopping Webserver")
    (when-let [close-fn (::server this)]
      (close-fn))))

(defn new-server []
  (->WebServer {:verbose? true
                :port 8080
                :max-body
                (* 16 1024 1024)}))
