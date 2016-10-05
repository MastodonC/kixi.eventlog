(defproject kixi.eventlog "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure                           "1.7.0"]
                 [org.clojure/core.async                        "0.1.346.0-17112a-alpha"]
                 [joda-time                                     "2.8.2"]
                 [com.fasterxml.jackson.core/jackson-databind   "2.6.2"]
                 [amazonica                                     "0.3.33"
                  :exclusions [joda-time
                               com.fasterxml.jackson.core/jackson-core
                               com.fasterxml.jackson.core/jackson-annotations]]
                 [mastodonc/clj-kafka                           "0.2.6-0.8.2.2"
                  :exclusions [org.slf4j/slf4j-log4j12]]
                 [compojure                                     "1.4.0"]

                 [commons-codec                                 "1.10"] ;; FIXME - ring-defaults needs this, but doesn't declare it?

                 [ring/ring-defaults                            "0.1.5"]

                 [org.clojure/tools.cli                         "0.3.3"]
                 ;; tools.trace for liberator
                 [org.clojure/tools.trace                       "0.7.8"]
                 [liberator                                     "0.13"]

                 [http-kit                                      "2.1.19"]

                 [com.stuartsierra/component                    "0.3.0"]

                 [prismatic/schema                              "1.0.1"]
                 [org.clojure/tools.nrepl                       "0.2.11"]
                 [cider/cider-nrepl                             "0.9.1"]

                 ;; Logging
                 [org.clojure/tools.logging                     "0.3.1"]
                 [ch.qos.logback/logback-classic                "1.1.3"]
                 [org.slf4j/jul-to-slf4j                        "1.7.12"]
                 [org.slf4j/jcl-over-slf4j                      "1.7.12"]
                 [org.slf4j/log4j-over-slf4j                    "1.7.12"]
                 [net.logstash.logback/logstash-logback-encoder "4.6"]
                 [buddy "1.0.0"]
                 [clj-http "2.3.0"]
                 [clj-time "0.12.0"]
                 [aero "1.0.1"]]

  :java-source-paths ["java-src"]
  :javac-options ["-target" "1.7" "-source" "1.7" "-Xlint:-options"]

  :main ^:skip-aot kixi.eventlog.Bootstrap
  :repl-options {:init-ns user}

  :plugins [[com.palletops/uberimage "0.3.0"]]

  :uberimage {:base-image "mastodonc/basejava"
              :instructions ["EXPOSE 4001"]
              :cmd ["/bin/bash" "/start-eventlog"]
              :files {"start-eventlog" "docker/start-eventlog.sh"}
              :tag "mastodonc/kixi.eventlog"}

  :profiles {:dev {:dependencies [[lein-marginalia "0.8.0"]
                                  [org.clojure/tools.namespace "0.2.10"]]
                   :plugins [[com.palletops/uberimage "0.3.0"]]
                   :source-paths ["dev" "src"]
                   :resource-paths ["dev-resources" "resources"]}})
