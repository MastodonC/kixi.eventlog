(defproject kixi.eventlog "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure                           "1.8.0"]
                 [org.clojure/core.async                        "0.2.391"]
                 [joda-time                                     "2.8.2"]
                 [com.fasterxml.jackson.core/jackson-databind   "2.6.2"]
                 [amazonica                                     "0.3.33"
                  :exclusions [joda-time
                               com.fasterxml.jackson.core/jackson-core
                               com.fasterxml.jackson.core/jackson-annotations]]
                 [com.taoensso/timbre                           "4.8.0"]
                 [mastondonc/franzy                             "0.0.3"]
                 [lbradstreet/franzy-admin                      "0.0.2"
                  :exclusions [com.taoensso/timbre]]
                 [compojure                                     "1.4.0"]

                 [commons-codec                                 "1.10"] ;; FIXME - ring-defaults needs this, but doesn't declare it?

                 [ring/ring-defaults                            "0.1.5"]

                 [org.clojure/tools.cli                         "0.3.3"]
                 ;; tools.trace for liberator
                 [org.clojure/tools.trace                       "0.7.8"]
                 [liberator                                     "0.13"]

                 [http-kit                                      "2.1.19"]

                 [com.stuartsierra/component                    "0.3.0"]

                 [prismatic/schema                              "1.1.3"]
                 [org.clojure/tools.nrepl                       "0.2.11"]
                 [cider/cider-nrepl                             "0.9.1"]
                 [buddy "1.0.0"]
                 [clj-http "2.3.0"]
                 [clj-time "0.12.0"]
                 [aero "1.0.1"]
                 [ring-basic-authentication "1.0.5"]]

  :java-source-paths ["java-src"]
  :javac-options ["-target" "1.8" "-source" "1.8" "-Xlint:-options"]

  :main ^:skip-aot kixi.eventlog.JavaBootstrap
  :repl-options {:init-ns user}

  :plugins [[com.palletops/uberimage "0.3.0"]]

  :profiles {:uberjar {:aot [kixi.eventlog.bootstrap]
                       :main kixi.eventlog.bootstrap
                       :uberjar-name "kixi.eventlog.jar"}
             :dev {:dependencies [[lein-marginalia "0.8.0"]
                                  [org.clojure/tools.namespace "0.2.10"]]
                   :source-paths ["dev" "src"]
                   :resource-paths ["dev-resources" "resources"]}})
