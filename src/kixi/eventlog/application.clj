(ns kixi.eventlog.application
  (:require [com.stuartsierra.component :as component]
            [kixi.eventlog.web-server :as web]))

(def instance)

(defrecord EventLogApi []
  component/Lifecycle
  (start [this]
    (println "Starting EventLogApi")
    (component/start-system this (keys this)))
  (stop [this]
    (println "Stopping EventLogApi")
    (component/stop-system this (keys this))))

(defn new-system
  ([] (let []
       (-> (map->EventLogApi
            {:web-server  (web/new-server)
             :repl-server (Object.) ; dummy - replaced when invoked via controller.main
             })
           (component/system-using
            {}))))
  ([extra-components]
     (merge (new-system) extra-components)))
