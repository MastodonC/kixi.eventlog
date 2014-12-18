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
  (start [this]
    (log/info "Starting EventProducer")
    (assoc this :instance
           (p/producer {"metadata.broker.list" (zk/broker-list (:zookeeper this))
                        "serializer.class" "kafka.serializer.DefaultEncoder"
                        "partitioner.class" "kafka.producer.DefaultPartitioner"})))
  (stop [this]
    (log/info "Stopping EventProducer")
    (when-let [i (:instance this)] (.close i))
    (dissoc this :instance)))

(defn new-producer []
  (->EventProducer))
