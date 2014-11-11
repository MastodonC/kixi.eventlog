(ns kixi.event.consumer
  (:require [clj-kafka.core :as kafka]
            [clj-kafka.consumer.zk :as c]
            [kixi.event.topic :refer [create-topic]]
            [com.stuartsierra.component :as component]
            [kixi.event.zookeeper :as zk]
            [clojure.tools.logging :as log]))

(defrecord EventConsumer []
  component/Lifecycle
  (start [this]
    (println "Starting EventConsumer"))
  (stop [this]
    (println "Stopping EventConsumer")
    (when-let [i (::instance this)] (.close i))
    (dissoc this ::instance)))

(defn new-consumer []
  (->EventConsumer))
