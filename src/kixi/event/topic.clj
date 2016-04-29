(ns kixi.event.topic
  (:require [clj-kafka.consumer.zk :as c]
            [clj-kafka.core :as kafka]
            [clj-kafka.producer :as p]
            [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]))


(defn topic-exists? [zk-client topic-name]
  (kafka.admin.AdminUtils/topicExists zk-client topic-name))

(defn- create-topic [zk-client topic-name {:keys [num-partitions replication-factor topic-config] :as opts} ]
  (log/infof "Creating topic %s, config: %s" topic-name (pr-str opts))
  (kafka.admin.AdminUtils/createTopic zk-client
                                      topic-name
                                      num-partitions
                                      replication-factor
                                      (kafka/as-properties topic-config)))

(defrecord EventTopics [topic-defs]
  component/Lifecycle
  (start [this]
    (log/info "Starting EventTopics " (:topic-defs this))
    (let [zk-connect (get-in (:zookeeper this) [:opts "zookeeper.connect"])
          zk-client  (org.I0Itec.zkclient.ZkClient. zk-connect
                                                    10000 ; sessionTimeoutMs
                                                    10000 ; connectionTimeoutMs
                                                    kafka.utils.ZKStringSerializer$/MODULE$)]
      (try 
        (doseq [[topic-name opts] (:topic-defs this)
                :when (not (topic-exists? zk-client topic-name))]
          (create-topic zk-client topic-name opts))
        (finally
          (.close zk-client))))
    this)
  (stop [this]
    (log/info "Stopping EventTopics" (:name this))
    this))

(defn publish [producer topic-name event]
  (log/info "producer:" producer )
  (log/info "topic-name:" topic-name)
  (log/info "event:" event)
  (p/send-message (:kixi.event.producer/instance producer) (p/message topic-name (.bytes event))))

(defn new-topics 
  ([topic-defs]
   (map->EventTopics {:topic-defs topic-defs})))
