(ns kixi.event.zookeeper
  (:require [clj-kafka.zk]
            [com.stuartsierra.component :as component]
            [zookeeper :as zk]
            [clojure.tools.logging :as log]))

(defrecord ZookeeperClient [opts]
  component/Lifecycle
  (start [this]
    (log/info "Starting ZookeeperClient")
    this)
  (stop [this]
    (log/info "Stopping EventClient")))

(defn connect [{:keys [opts]}]
  (zk/connect (get opts "zookeeper.connect")))

(defn close [zkc]
  (.close zkc))

(defn broker-list [{:keys [opts]}]
  (clj-kafka.zk/broker-list (clj-kafka.zk/brokers opts)))

(defn new-zk-client [host port]
  (->ZookeeperClient {"zookeeper.connect" (format "%s:%d" host port)}))
