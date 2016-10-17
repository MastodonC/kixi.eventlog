(ns kixi.eventlog.bootstrap
  (:require [cider.nrepl                :refer (cider-nrepl-handler)]
            [clojure.tools.cli          :refer [cli]]
            [clojure.tools.logging      :as log]
            [clojure.tools.nrepl.server :as nrepl-server]
            [com.stuartsierra.component :as component]
            [kixi.eventlog.application  :as kixi])
  (:gen-class))


(defrecord ReplServer [config]
  component/Lifecycle
  (start [this]
    (log/info "Starting REPL server " config)
    (assoc this :repl-server
           (apply nrepl-server/start-server :handler cider-nrepl-handler (flatten (seq config)))))
  (stop [this]
    (log/info "Stopping REPL server with " config)
    (nrepl-server/stop-server (:repl-server this))
    (dissoc this :repl-server)))

(defn mk-repl-server [config]
  (ReplServer. config))

(defn build-application [system opts]
  (-> system
      (cond-> (:repl opts)
        (assoc :repl-server (mk-repl-server {:port (:repl-port opts)})))))

(defn main [args]
  (log/info "Starting kixi.eventlog")
  (let [[opts args banner]
        (cli args
             ["-h" "--help" "Show help"
              :flag true :default false]
             ["-R" "--repl" "Start a REPL"
              :flag true :default true]
             ["-r" "--repl-port" "REPL server listen port"
              :default 4001 :parse-fn #(Integer/valueOf %)]
             ["-P" "--profile" "Environment profile"
              :default :development :parse-fn keyword]
             ["-a" "--authentication" "Do we want authentication?"
              :flag true :default false])]
    (when (:help opts)
      (log/info banner)
      (System/exit 0))
    (alter-var-root #'kixi/instance (fn [_]
                                      (component/start
                                       (build-application
                                        (kixi/new-system opts)
                                        opts))))))
