(ns kixi.event.topic
  (:require [clj-kafka.core :as kafka]
            [clj-kafka.producer :as p]
            [clj-kafka.consumer.zk :as c]
            [com.stuartsierra.component :as component]
            [clojure.tools.logging :as log]))

;; From http://stackoverflow.com/questions/16946778/how-can-we-create-a-topic-in-kafka-from-the-ide-using-api

;; seems overly complex, but works...

(defn create-topic [{:keys [zookeeper num-partitions replication-factor topic-config]} topic-name ]
  (let [zk-connect (get-in zookeeper[:opts "zookeeper.connect"])
        client     (org.I0Itec.zkclient.ZkClient. zk-connect
                                                  10000 ; sessionTimeoutMs
                                                  10000 ; connectionTimeoutMs
                                                  kafka.utils.ZKStringSerializer$/MODULE$)]
    (try
      (kafka.admin.AdminUtils/createTopic client
                                          topic-name
                                          num-partitions
                                          replication-factor
                                          (kafka/as-properties topic-config))
      (finally
        (.close client)))))

(defrecord EventTopic [name]
  component/Lifecycle
  (start [this]
    (log/info "Starting EventTopic " (:name this))
    (try
      (create-topic this name)
      (catch kafka.common.TopicExistsException _
        (log/info (:name this) " topic already exists")))
    this)
  (stop [this]
    (log/info "Stopping EventTopic" (:name this))
    this))

(defn publish [topic event]
  (let [producer (-> topic :producer :instance)
        topic-name (-> topic :name)]
    (p/send-message producer (p/message topic-name (.bytes event)))))

(defn new-topic [name & {:as opts}]
  (map->EventTopic (assoc opts
                     :name name)))
