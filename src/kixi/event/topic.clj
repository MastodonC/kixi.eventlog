(ns kixi.event.topic)

;; From http://stackoverflow.com/questions/16946778/how-can-we-create-a-topic-in-kafka-from-the-ide-using-api

;; seems overly complex, but works...

(defn create-topic [{:keys [opts]} topic-name & args]
  (let [{:keys [num-partitions replication-factor topic-config]
         :or {num-partitions 3 replication-factor 2 topic-config (java.util.Properties.)}} opts
         client (org.I0Itec.zkclient.ZkClient. (get opts "zookeeper.connect")
                                               10000 ; sessionTimeoutMs
                                               10000 ; connectionTimeoutMs
                                               kafka.utils.ZKStringSerializer$/MODULE$)]
    (try
      (kafka.admin.AdminUtils/createTopic client topic-name num-partitions replication-factor topic-config)
      (finally
        (.close client)))))
