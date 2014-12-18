(ns kixi.event.consumer
  (:require [clojure.core.async :refer (chan <! >! thread go-loop timeout alts! close! put! <!! >!!)]
            [clj-kafka.core :as kafka]
            [clj-kafka.consumer.zk :as c]
            [com.stuartsierra.component :as component]
            [kixi.event.zookeeper :as zk]
            [clojure.tools.logging :as log]))

(defrecord EventConsumer [consumer-config batch-size]
  component/Lifecycle
  (start [{:keys [zookeeper] :as this}]
    (log/info "Starting EventConsumer")
    (let [consumer        (c/consumer (merge consumer-config (:opts zookeeper)))
          topic-name      (-> this :topic :name)
          topic-count-map {topic-name (:thread-count consumer-config)}
          streams         (-> (.createMessageStreams topic-count-map)
                              (get topic-name))]
      (doseq [stream streams]
        (thread
          (c/messages )
          )
        )
      )
    (assoc this ::consumer ))
  (stop [this]
    (log/info "Stopping EventConsumer")
    (when-let [i (::instance this)] (.shutdown i))
    (dissoc this ::consumer)))

(defn new-consumer [batch-size]
  (->EventConsumer batch-size))
