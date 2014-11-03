(ns kixi.eventlog.web-server
  (:require [compojure.core :refer [defroutes POST GET]]
            [compojure.route :refer [not-found]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [kixi.eventlog.api :refer [index-resource]]
            [org.httpkit.server :as http-kit]
            [com.stuartsierra.component :as component]))

(defroutes all-routes
  (POST "/events" []  index-resource)
  (not-found {:headers {"Content-Type" "application/json"}
              :body "{\"error\": \"No Such Endpoint\"}"}))

(defrecord WebServer []
  component/Lifecycle
  (start [this]
    (println "Starting Webserver")
    (let [server (http-kit/run-server (wrap-defaults #'all-routes api-defaults) {:verbose? true :port 80 :max-body (* 16 1024 1024)})]
      (assoc this ::server server)))
  (stop [this]
    (println "Stopping Webserver")
    ((::server this))))

(defn new-server []
  (->WebServer))
