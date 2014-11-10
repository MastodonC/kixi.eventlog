(ns kixi.event.producer
 (:require [clj-kafka.core :as kafka]
           [clj-kafka.producer :as p]
           [kixi.event.topic :refer [create-topic]]
           [com.stuartsierra.component :as component]
           [kixi.event.zookeeper :as zk]
           [clojure.tools.logging :as log])
 (:import [kafka.admin AdminUtils]))

(defprotocol IPublish
  (-publish [topic event]))

(defrecord EventProducer []
  component/Lifecycle
  (start [this]
    (println "Starting EventProducer")
    (assoc this ::instance
           (p/producer {"metadata.broker.list" (zk/broker-list (:zookeeper this))
                        "serializer.class" "kafka.serializer.DefaultEncoder"
                        "partitioner.class" "kafka.producer.DefaultPartitioner"})))
  (stop [this]
    (println "Stopping EventProducer")
    (when-let [i (::instance this)] (.close i))))

(defrecord EventTopic [name]
  component/Lifecycle
  (start [this]
    (println "Starting EventTopic " (:name this) ":" (:zookeeper this))
    (try
      (create-topic (:zookeeper this) name)
      (catch kafka.common.TopicExistsException _
        (log/info "events topic already exists"))))
  (stop [this]
    (println "Stopping EventTopic" (:name this)))
  IPublish
  (-publish [this event]
    (p/send-message (-> this :producer ::instance) (:name this) (.getBytes event))))


(defn publish [topic event]
  (-publish topic event))

(defn new-producer []
  (->EventProducer))

(defn new-topic [name]
  (->EventTopic name))
