(ns kixi.eventlog.application
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [kixi.eventlog.web-server :as web]
            [kixi.event.producer :refer [new-producer]]
            [kixi.event.topic :refer [new-topics]]
            [kixi.event.zookeeper :refer [new-zk-client]]))

(def instance)

(defrecord EventLogApi []
  component/Lifecycle
  (start [this]
    (log/info "Starting EventLogApi")
    (component/start-system this (keys this)))
  (stop [this]
    (log/info "Stopping EventLogApi")
    (component/stop-system this (keys this))))

(defn topic-definitions [max-message-size topic-names]
  (zipmap topic-names
          (map (fn [topic-name] 
                 {:num-partitions     (Integer/valueOf
                                       (or (System/getenv (str (.toUpperCase topic-name) "_TOPIC_NUM_PARTITIONS"))
                                           "3"))
                  :replication-factor (Integer/valueOf 
                                       (or (System/getenv (str (.toUpperCase topic-name) "_TOPIC_REPLICATION_FACTOR"))
                                           "3") )
                  :max-message-size   max-message-size}) topic-names)))

(defn parse-topics [topics]
  (re-seq #"[\w-]+" topics))

(defn new-system
  ([] (let [zookeeper-connect (or (System/getenv "ZK_CONNECT") "localhost:2181")
            max-message-size  (or (System/getenv "TOPIC_MAX_MESSAGE_SIZE") "1000000")
            producer          (new-producer :max-message-size max-message-size)
            topic-names       (or (System/getenv "TOPICS")
                                  "flight_events01 hotel_events01")
            topics            (new-topics 
                               (topic-definitions max-message-size 
                                                  (parse-topics topic-names)))]
        (-> (map->EventLogApi
            {:web-server  (web/new-server)
             :repl-server (Object.) ; dummy - replaced when invoked via controller.main
             :zookeeper   (new-zk-client zookeeper-connect)
             :topics      topics
             :producer    producer})
           (component/system-using
            {:producer   [:zookeeper]
             :topics     [:zookeeper]
             :web-server [:producer :topics]}))))
  ([extra-components]
     (merge (new-system) extra-components)))
