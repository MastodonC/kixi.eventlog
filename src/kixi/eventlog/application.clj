(ns kixi.eventlog.application
  (:require [com.stuartsierra.component :as component]
            [kixi.eventlog.web-server :as web]
            [kixi.event.zookeeper :refer [new-zk-client]]
            [kixi.event.producer :refer [new-producer new-topic]]
            ))

(def instance)

(defrecord EventLogApi []
  component/Lifecycle
  (start [this]
    (println "Starting EventLogApi")
    (component/start-system this (keys this)))
  (stop [this]
    (println "Stopping EventLogApi")
    (component/stop-system this (keys this))))

(defn new-system
  ([] (let []
       (-> (map->EventLogApi
            {:web-server   (web/new-server)
             :repl-server  (Object.) ; dummy - replaced when invoked via controller.main
             :zookeeper    (new-zk-client "localhost" 2181)
             :producer     (new-producer)
             :events-topic (new-topic "events")})
           (component/system-using
            {:producer [:zookeeper]
             :events-topic [:producer :zookeeper]
             :web-server {:topic :events-topic}}))))
  ([extra-components]
     (merge (new-system) extra-components)))
