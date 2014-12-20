(ns kixi.event.producer
 (:require [clj-kafka.core :as kafka]
           [clj-kafka.producer :as p]
           [kixi.event.zookeeper :as zk]
           [clojure.tools.logging :as log]
           [com.stuartsierra.component :as component]
           [clojure.tools.logging :as log])
 (:import [kafka.admin AdminUtils]))

(defrecord EventProducer []
  component/Lifecycle
  (start [{:keys [zookeeper max-message-size] :as this}]
    (log/info "Starting EventProducer")
    (log/info "  Zookeeper is: " zookeeper)
    (log/info "  Max message size is: " max-message-size)
    (assoc this :instance
           (p/producer {"metadata.broker.list" (zk/broker-list zookeeper)
                        "serializer.class" "kafka.serializer.DefaultEncoder"
                        "partitioner.class" "kafka.producer.DefaultPartitioner"
                        "max.message.size" (str max-message-size)
                        "compression.codec" "1"})))
  (stop [this]
    (log/info "Stopping EventProducer")
    (when-let [i (:instance this)] (.close i))
    (dissoc this :instance)))

(defn new-producer
  ([] (->EventProducer))
  ([& {:as opts}]
    (map->EventProducer opts)))
