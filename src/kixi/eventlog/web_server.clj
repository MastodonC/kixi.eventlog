(ns kixi.eventlog.web-server
  (:require [clojure.string :as str]
            [taoensso.timbre :as log]
            [compojure.core :refer [make-route routes POST GET defroutes wrap-routes]]
            [compojure.route :refer [not-found]]
            [com.stuartsierra.component :as component]
            [kixi.eventlog.api :refer [index-resource]]
            [kixi.event.topic :refer [publish]]
            [org.httpkit.server :as http-kit]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [buddy.sign.jwt :as jwt]
            [kixi.eventlog.authz :as authz]))

(defroutes status-routes
  (GET "/_elb_status" []  "ALL GOOD")
  (POST "/_elb_status" []  "ALL GOOD"))

(defn path-for-topic [topic-name]
  (let [name (if-let [[name _] (next (re-matches #"(\w+?)(\d+)" topic-name))]
               name
               topic-name)
        path (str "/" (str/replace name #"_" "/"))]
    (log/infof "Path for topic %s is %s" topic-name path)
    path))

(defn topic-routes [component]
  (let [topic-routes (map #(POST (path-for-topic %) []
                                 (index-resource (partial publish (:producer component) %))) (-> component :topics :topic-defs keys))]
    (apply routes topic-routes)))

(defn app
  [component authentication? auth]
  (routes
   status-routes
   (wrap-routes  (topic-routes component) (authz/maybe-wrap-authentication authentication? auth))
   (not-found {:headers {"Content-Type" "application/json"}
               :body    "{\"error\": \"No Such Endpoint\"}"})))

(defrecord WebServer [opts authentication? auth]
  component/Lifecycle
  (start [this]
    (log/info "Starting Webserver")
    (let [server (http-kit/run-server (wrap-defaults
                                       (app this authentication? auth)
                                       api-defaults)
                                      opts)]
      (assoc this ::server server)))
  (stop [this]
    (log/info "Stopping Webserver")
    (when-let [close-fn (::server this)]
      (close-fn))))

(defn new-server [authentication? auth max-message-size]
  (->WebServer {:verbose? true
                :port 8080
                :max-body
                max-message-size}
               authentication?
               auth))
