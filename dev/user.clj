(ns user
  (:require ;; DO NOT ADD ANYTHING HERE THAT MIGHT REMOTELY HAVE A COMPILATION ERROR.
   ;; THIS IS TO ENSURE WE CAN ALWAYS GET A REPL STARTED.
   ;;
   ;; see (init) below.

   [com.stuartsierra.component :as component]
   [clojure.pprint :refer (pprint)]
   [clojure.reflect :refer (reflect)]
   [clojure.repl :refer (apropos dir doc find-doc pst source)]
   [clojure.tools.namespace.repl :refer [refresh refresh-all]]))

(defn init
  "Constructs the current development system."
  []
  ;; We do some gymnastics here to make sure that the REPL can always start
  ;; even in the presence of compilation errors.
  (require '[kixi.eventlog.application])

  (let [new-system (resolve 'kixi.eventlog.application/new-system)
        instance (resolve 'kixi.eventlog.application/instance)]
    (alter-var-root instance
                    (constantly (new-system :development)))))

(defn start
  "Starts the current development system."
  []
  (alter-var-root (resolve 'kixi.eventlog.application/instance) component/start))

(defn stop
  "Shuts down and destroys the current development system."
  []
  (alter-var-root (resolve 'kixi.eventlog.application/instance) (fn [s] (when s (component/stop s)))))

(defn go
  "Initializes the current development system and starts it running."
  []
  (init)
  (start)
  nil)

(defn reset []
  (stop)
  (refresh :after 'user/go)
  nil)
