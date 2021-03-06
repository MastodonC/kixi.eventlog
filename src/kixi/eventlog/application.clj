(ns kixi.eventlog.application
  (:require [taoensso.timbre :as log]
            [clojure.java.io :as io]
            [com.stuartsierra.component :as component]
            [kixi.eventlog.web-server :as web]
            [kixi.event.producer :refer [new-producer]]
            [kixi.event.topic :refer [new-topics]]
            [aero.core :as aero]))

(def instance)

(defrecord EventLogApi []
  component/Lifecycle
  (start [this]
    (log/info "Starting EventLogApi")
    (component/start-system this (keys this)))
  (stop [this]
    (log/info "Stopping EventLogApi")
    (component/stop-system this (keys this))))

(defn topic-definitions [max-message-size topic-names]
  (zipmap topic-names
          (map (fn [topic-name]
                 {:num-partitions     (Integer/valueOf
                                       (or (System/getenv (str (.toUpperCase topic-name) "_TOPIC_NUM_PARTITIONS"))
                                           "1"))
                  :replication-factor (Integer/valueOf
                                       (or (System/getenv (str (.toUpperCase topic-name) "_TOPIC_REPLICATION_FACTOR"))
                                           "1") )
                  :max-message-size   max-message-size}) topic-names)))

(defn parse-topics [topics]
  (re-seq #"[\w-]+" topics))

(defn config
  [profile]
  (try (aero/read-config (io/resource "eventlog.edn") {:resolver aero/relative-resolver :profile profile})
       (catch java.io.FileNotFoundException _ (log/info "no authentication config found"))))

(defn new-system
  ([{:keys [profile authentication topics]}]
   (let [config            (config profile)
         zookeeper-connect (:zookeeper config)
         max-message-size  (or (System/getenv "TOPIC_MAX_MESSAGE_SIZE") (str (* 16 1024 1024)))
         producer          (new-producer :max-message-size max-message-size
                                         :zookeeper zookeeper-connect)
         topic-names       (or (System/getenv "TOPICS") topics)
         topics            (new-topics
                            :topic-defs (topic-definitions max-message-size
                                                           (parse-topics topic-names))
                            :zookeeper zookeeper-connect)
         auth              (:auth config)]
     (-> (map->EventLogApi
          {:web-server  (web/new-server authentication auth (Integer/valueOf max-message-size))
           :repl-server (Object.) ; dummy - replaced when invoked via controller.main
           :topics      topics
           :producer    producer})
         (component/system-using
          {:web-server [:producer :topics]}))))
  ([opts extra-components]
   (merge (new-system opts) extra-components)))
