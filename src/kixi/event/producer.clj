(ns kixi.event.producer
  (:require [taoensso.timbre :as log]
            [com.stuartsierra.component :as component]
            [clojure.core.async :as async]
            [franzy.clients.producer
             [client :as producer]
             [defaults :as pd]
             [protocols :as pp]]
            [franzy.serialization
             [deserializers :as deserializers]
             [serializers :as serializers]]
            [franzy.admin.zookeeper.defaults :as zk-defaults]
            [franzy.admin.zookeeper.client :as client]
            [franzy.admin.cluster :as cluster]))

(defn uuid
  []
  (str (java.util.UUID/randomUUID)))

(defn get-broker-list
  [zk-conf]
  (let [c (merge (zk-defaults/zk-client-defaults) zk-conf)]
    (with-open[u (client/make-zk-utils c false)]
      (cluster/all-brokers u))))

(defn create-producer
  [in-chan broker-list]
  (let [key-serializer     (serializers/string-serializer)
        value-serializer   (serializers/byte-array-serializer)
        pc                 {:bootstrap.servers broker-list}
        po                 (pd/make-default-producer-options)]
    (try
      (let [producer           (producer/make-producer
                                pc
                                key-serializer
                                value-serializer
                                po)]
        (async/go
          (loop []
            (let [msg (async/<! in-chan)]
              (if msg
                (let [{:keys [topic payload]} msg]
                  (pp/send-sync! producer topic nil
                                 (uuid)
                                 payload
                                 po)
                  (recur))
                (.close producer))))))
      (catch Exception e
        (log/error e (str "Producer Exception1, pc=" pc ", po=" po))))))

(defrecord EventProducer []
  component/Lifecycle
  (start [{:keys [zookeeper max-message-size] :as this}]
    (log/info "Starting EventProducer")
    (log/info "  Zookeeper is: " zookeeper)
    (let [in-chan (async/chan)
          brokers (get-broker-list {:servers zookeeper})
          broker-list (clojure.string/join
                       ","
                       (map #(->> (get-in % [:endpoints :plaintext])
                                  ((juxt :host :port))
                                  (clojure.string/join ":")) brokers))]
      (assoc this
             ::instance (create-producer in-chan broker-list)
             ::chan in-chan)))
  (stop [this]
    (log/info "Stopping EventProducer")
    (when-let [i (::chan this)] (.close i))
    (dissoc this
            ::instance
            ::chan)))

(defn new-producer
  ([] (->EventProducer))
  ([& {:as opts}]
    (map->EventProducer opts)))
