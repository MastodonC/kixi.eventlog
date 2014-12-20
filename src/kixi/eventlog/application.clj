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
  ([] (let [zookeeper-connect     (or (System/getenv "ZK_CONNECT") "localhost:2181")
            topic              (or (System/getenv "TOPIC") "events")
            num-partitions     (Integer/parseInt (or (System/getenv "TOPIC_NUM_PARTITIONS") "3"))
            replication-factor (Integer/parseInt (or (System/getenv "TOPIC_REPLICATION_FACTOR") "3"))
            max-message-size   (or (System/getenv "TOPIC_MAX_MESSAGE_SIZE") "1000000")] ;; annoying inconsistency Int vs String.
       (-> (map->EventLogApi
            {:web-server   (web/new-server)
             :repl-server  (Object.) ; dummy - replaced when invoked via controller.main
             :zookeeper    (new-zk-client zookeeper-connect)
             :producer     (new-producer :max-message-size max-message-size)
             :events-topic (new-topic topic
                                      :num-partitions num-partitions
                                      :replication-factor replication-factor
                                      :topic-config {})})
           (component/system-using
            {:producer [:zookeeper]
             :events-topic [:producer :zookeeper]
             :web-server {:topic :events-topic}}))))
  ([extra-components]
     (merge (new-system) extra-components)))
