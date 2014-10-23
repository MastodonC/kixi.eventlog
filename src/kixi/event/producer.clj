(ns kixi.event.producer
  (:require [clj-kafka.zk :as zk]
            [clj-kafka.producer :as p]))


;; ASSUMES:

;; bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic test



;; (brokers {"zookeeper.connect" "127.0.0.1:2181"})
;; ;; ({:host "localhost", :jmx_port -1, :port 9999, :version 1})
;; (use 'clj-kafka.producer)

(def p1 (p/producer {"metadata.broker.list" "localhost:9092"
                  "serializer.class" "kafka.serializer.DefaultEncoder"
                  "partitioner.class" "kafka.producer.DefaultPartitioner"}))

(p/send-message p1 (p/message "test" (.getBytes "this is my message")))
