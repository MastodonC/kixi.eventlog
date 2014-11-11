(ns kixi.event.topic
  (:require [clj-kafka.producer :as p]
            [clj-kafka.consumer.zk :as c]
            [com.stuartsierra.component :as component]
            [clojure.tools.logging :as log]))

;; From http://stackoverflow.com/questions/16946778/how-can-we-create-a-topic-in-kafka-from-the-ide-using-api

;; seems overly complex, but works...

(defn create-topic [{:keys [opts]} topic-name & args]
  (let [{:keys [num-partitions replication-factor topic-config]
         :or {num-partitions 3 replication-factor 2 topic-config (java.util.Properties.)}} opts
         client (org.I0Itec.zkclient.ZkClient. (get opts "zookeeper.connect")
                                               10000 ; sessionTimeoutMs
                                               10000 ; connectionTimeoutMs
                                               kafka.utils.ZKStringSerializer$/MODULE$)]
    (try
      (kafka.admin.AdminUtils/createTopic client topic-name num-partitions replication-factor topic-config)
      (finally
        (.close client)))))

(defprotocol IPublish
  (-publish [topic event]))

(defprotocol IConsume
  (-consume [topic]))

(defrecord EventTopic [name]
  component/Lifecycle
  (start [this]
    (println "Starting EventTopic " (:name this))
    (try
      (create-topic (:zookeeper this) name)
      (catch kafka.common.TopicExistsException _
        (log/info "events topic already exists")))
    this)
  (stop [this]
    (println "Stopping EventTopic" (:name this))
    this)
  IPublish
  (-publish [this event]
    (p/send-message (-> this :producer ::instance) (p/message (:name this) (.bytes event)))))

(defn consume [topic n]
  (-consume topic))

(defn new-topic [name]
  (->EventTopic name))
