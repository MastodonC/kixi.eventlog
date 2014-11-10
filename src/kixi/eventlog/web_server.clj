(ns kixi.eventlog.web-server
  (:require [compojure.core :refer [routes POST GET]]
            [compojure.route :refer [not-found]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [kixi.eventlog.api :refer [index-resource]]
            [org.httpkit.server :as http-kit]
            [com.stuartsierra.component :as component]))

(defn all-routes [producer]
  (routes
   (POST "/events" [] (index-resource producer))
   (GET "/_elb_status" []  "ALL GOOD")
   (POST "/_elb_status" []  "ALL GOOD")
   (not-found {:headers {"Content-Type" "application/json"}
               :body "{\"error\": \"No Such Endpoint\"}"})))

(defrecord WebServer []
  component/Lifecycle
  (start [this]
    (println "Starting Webserver")
    (let [server (http-kit/run-server (wrap-defaults (all-routes (:producer this)) api-defaults) {:verbose? true :port 80 :max-body (* 16 1024 1024)})]
      (println server)
      (assoc this ::server server)))
  (stop [this]
    (println "Stopping Webserver")
    (when-let [close-fn (::server this)]
      (close-fn))))

(defn new-server []
  (->WebServer))
