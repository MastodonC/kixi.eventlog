(ns kixi.event.topic
  (:require [taoensso.timbre :as log]
            [com.stuartsierra.component :as component]
            [franzy.admin.zookeeper.defaults :as zk-defaults]
            [franzy.admin.zookeeper.client :as client]
            [franzy.admin.topics :as topics]
            [clojure.core.async :as async]))


(defn topic-exists? [zk-client topic-name]
  (topics/topic-exists? zk-client topic-name))

(defn- create-topic [zk-client topic-name {:keys [num-partitions replication-factor topic-config] :as opts} ]
  (log/infof "Creating topic %s, config: %s" topic-name (pr-str opts))
  (topics/create-topic! zk-client
                        topic-name
                        num-partitions
                        replication-factor
                        topic-config))

(defrecord EventTopics [topic-defs zookeeper]
  component/Lifecycle
  (start [this]
    (log/info "Starting EventTopics " topic-defs)
    (let [zku-conf (merge (zk-defaults/zk-client-defaults) {:servers zookeeper})
          zk-utils (client/make-zk-utils zku-conf false)]
      (try
        (doseq [[topic-name opts] (:topic-defs this)
                :when (not (topic-exists? zk-utils topic-name))]
          (create-topic zk-utils topic-name opts))
        (finally
          (.close zk-utils))))
    this)
  (stop [this]
    (log/info "Stopping EventTopics" (:name this))
    this))

(defn publish [producer topic-name event]
  (log/info "producer:" producer )
  (log/info "topic-name:" topic-name)
  (log/info "event:" event)
  (async/go
    (async/>! (:kixi.event.producer/chan producer) {:topic topic-name :payload (.bytes event)}))
  nil)

(defn new-topics
  ([& {:as opts}]
   (map->EventTopics opts)))
