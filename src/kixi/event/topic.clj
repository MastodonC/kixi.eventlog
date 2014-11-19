(ns kixi.event.topic
  (:require [clj-kafka.core :as kafka]
            [clj-kafka.producer :as p]
            [clj-kafka.consumer.zk :as c]
            [com.stuartsierra.component :as component]
            [clojure.tools.logging :as log]))

;; From http://stackoverflow.com/questions/16946778/how-can-we-create-a-topic-in-kafka-from-the-ide-using-api

;; seems overly complex, but works...

(defn create-topic [{:keys [opts]} topic-name & args]
  (let [{:keys [num-partitions replication-factor topic-config]
         :or {num-partitions 3 replication-factor 2 topic-config {}}} opts
         client (org.I0Itec.zkclient.ZkClient. (get opts "zookeeper.connect")
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
    (println "Starting EventTopic " (:name this))
    (try
      (create-topic (:zookeeper this) name)
      (catch kafka.common.TopicExistsException _
        (log/info "events topic already exists")))
    this)
  (stop [this]
    (println "Stopping EventTopic" (:name this))
    this))

(defn publish [topic event]
  (println "TT:" topic)
  (let [producer (-> topic :producer :instance)
        topic-name (-> topic :name)]
    (log/info "p:" producer " t:" topic-name)
    (p/send-message producer (p/message topic-name (.bytes event)))))

(defn new-topic [name]
  (->EventTopic name))
