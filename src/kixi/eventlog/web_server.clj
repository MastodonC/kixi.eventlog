(ns kixi.eventlog.web-server
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log]
            [compojure.core :refer [make-route routes POST GET]]
            [compojure.route :refer [not-found]]
            [com.stuartsierra.component :as component]
            [kixi.eventlog.api :refer [index-resource]]
            [kixi.event.topic :refer [publish]]
            [org.httpkit.server :as http-kit]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [buddy.sign.jwt :as jwt]
            [kixi.eventlog.authz :as authz]))

(defn status-routes []
  (routes
   (GET "/_elb_status" []  "ALL GOOD")
   (POST "/_elb_status" []  "ALL GOOD")))

(defn path-for-topic [topic-name]
  
  (let [name (if-let [[name _] (next (re-matches #"(\w+?)(\d+)" topic-name))]
               name
               topic-name)
        path (str "/" (str/replace name #"_" "/"))]
    (log/infof "Path for topic %s is %s" topic-name path)
    path))

(defn topic-routes [producer topic-names]
  (let [topic-routes (map #(POST (path-for-topic %) []
                                 (index-resource (partial publish producer %))) topic-names)]
    (apply routes topic-routes)))

(defrecord WebServer [opts]
  component/Lifecycle
  (start [this]
    (log/info "Starting Webserver")
    (let [routes                       (routes
                                        (authz/maybe-wrap-authentication (:auth opts) (topic-routes (:producer this) (-> this :topics :topic-defs keys)))
                                        (status-routes)
                                        (not-found {:headers {"Content-Type" "application/json"}
                                                    :body    "{\"error\": \"No Such Endpoint\"}"}))]
      (let [server (http-kit/run-server (wrap-defaults
                                         routes
                                         api-defaults)
                                        opts)]
        (assoc this ::server server))))
  (stop [this]
    (log/info "Stopping Webserver")
    (when-let [close-fn (::server this)]
      (close-fn))))

(defn new-server [auth]
  (->WebServer {:verbose? true
                :port 8080
                :max-body
                (* 16 1024 1024)
                :auth auth}))
