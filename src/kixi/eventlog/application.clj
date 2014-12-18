(ns kixi.eventlog.application
  (:require [com.stuartsierra.component :as component]
            [kixi.eventlog.web-server :as web]
            [kixi.event.zookeeper :refer [new-zk-client]]
            [kixi.event.producer :refer [new-producer]]
            [kixi.event.consumer :refer [new-consumer]]
            [kixi.event.topic :refer [new-topic]]
            [clojure.tools.logging :as log]))

(def instance)

(defrecord EventLogApi []
  component/Lifecycle
  (start [this]
    (log/info "Starting EventLogApi")
    (component/start-system this (keys this)))
  (stop [this]
    (log/info "Stopping EventLogApi")
    (component/stop-system this (keys this))))

(defn new-system
  ([] (let [zookeeper-host (or (System/getenv "ZK01_PORT_2181_TCP_ADDR") "localhost")
            zookeeper-port (Integer/valueOf (or (System/getenv "ZK01_PORT_2181_TCP_PORT") "2181"))
            topic (or (System/getenv "TOPIC") "events")
            num-partitions (or (System/getenv "TOPIC_NUM_PARTITIONS") 3)
            replication-factor (or (System/getenv "TOPIC_REPLICATION_FACTOR") 3)]
       (-> (map->EventLogApi
            {:web-server   (web/new-server)
             :repl-server  (Object.) ; dummy - replaced when invoked via controller.main
             :zookeeper    (new-zk-client zookeeper-host zookeeper-port)
             :producer     (new-producer)
             :events-topic (new-topic topic
                                      :num-partitions num-partitions
                                      :replication-factor replication-factor
                                      :topic-config {"compression.codec" 1 ;; gzip
                                                     })})
           (component/system-using
            {:producer [:zookeeper]
             :events-topic [:producer :zookeeper]
             :web-server {:topic :events-topic}}))))
  ([extra-components]
     (merge (new-system) extra-components)))
