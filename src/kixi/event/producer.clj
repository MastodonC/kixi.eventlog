(ns kixi.event.producer
  (:require [clj-kafka.core :as kafka]
            [clj-kafka.producer :as p]
            [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [kixi.event.zookeeper :as zk]))

(defrecord EventProducer []
  component/Lifecycle
  (start [{:keys [zookeeper max-message-size] :as this}]
    (log/info "Starting EventProducer")
    (log/info "  Zookeeper is: " zookeeper)
    (assoc this ::instance
           (p/producer {"metadata.broker.list" (zk/broker-list zookeeper)
                        "serializer.class"     "kafka.serializer.DefaultEncoder"
                        "partitioner.class"    "kafka.producer.DefaultPartitioner"})))
  (stop [this]
    (log/info "Stopping EventProducer")
    (when-let [i (::instance this)] (.close i))
    (dissoc this ::instance)))

(defn new-producer
  ([] (->EventProducer))
  ([& {:as opts}]
    (map->EventProducer opts)))
