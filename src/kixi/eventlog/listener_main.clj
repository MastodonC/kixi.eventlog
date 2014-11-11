(ns kixi.eventlog.webapp-main
  "Start up for application"
  (:gen-class)
  (:require [clojure.tools.cli          :refer [cli]]
            [clojure.tools.nrepl.server :as nrepl-server]
            [cider.nrepl                :refer (cider-nrepl-handler)]
            [kixi.eventlog.application  :as kixi]
            [com.stuartsierra.component :as component]
            [kixi.eventlog.main :refer :all]))

(defn -main [& args]
  (let [[opts args banner]
        (cli args
             ["-h" "--help" "Show help"
              :flag true :default false]
             ["-R" "--repl" "Start a REPL"
              :flag true :default true]
             ["-r" "--repl-port" "REPL server listen port"
              :default 4001 :parse-fn #(Integer. %)])]
    (when (:help opts)
      (println banner)
      (System/exit 0))
    (alter-var-root #'kixi/instance (fn [_]
                                      (component/start
                                       (build-application
                                        kixi/new-listener
                                        opts))))))
