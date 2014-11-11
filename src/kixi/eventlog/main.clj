(ns kixi.eventlog.main
    (:require [clojure.tools.nrepl.server :as nrepl-server]
            [cider.nrepl                :refer (cider-nrepl-handler)]
            [com.stuartsierra.component :as component]))

(defrecord ReplServer [config]
  component/Lifecycle
  (start [this]
    (println "Starting REPL server " config)
    (assoc this :repl-server
           (apply nrepl-server/start-server :handler cider-nrepl-handler (flatten (seq config)))))
  (stop [this]
    (println "Stopping REPL server with " config)
    (nrepl-server/stop-server (:repl-server this))
    (dissoc this :repl-server)))

(defn mk-repl-server [config]
  (ReplServer. config))

(defn build-application [system opts]
  (-> system
      (cond-> (:repl opts)
              (assoc :repl-server (mk-repl-server {:port (:repl-port opts)})))))
