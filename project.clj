(defproject kixi.eventlog "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure        "1.6.0"]
                 [joda-time                  "2.5"]
                 [com.fasterxml.jackson.core/jackson-databind "2.4.0"]
                 [amazonica                  "0.2.29" :exclusions [joda-time
                                                                   com.fasterxml.jackson.core/jackson-core
                                                                   com.fasterxml.jackson.core/jackson-annotations
                                                                   ]]
                 [mastodonc/clj-kafka        "0.2.6-0.8.1.1" :exclusions [org.slf4j/slf4j-log4j12]]
                 [compojure                  "1.2.1"]

                 [commons-codec              "1.9"] ;; FIXME - ring-defaults needs this, but doesn't declare it?

                 [ring/ring-defaults         "0.1.2"]

                 [org.clojure/tools.cli      "0.3.1"]
                 ;; tools.trace for liberator
                 [org.clojure/tools.trace    "0.7.8"]
                 [liberator                  "0.12.2"]

                 [http-kit                   "2.1.19"]

                 [com.stuartsierra/component "0.2.2"]

                 [prismatic/schema           "0.3.2"]
                 [org.clojure/tools.nrepl    "0.2.6"]
                 [cider/cider-nrepl          "0.7.0"]


                 ;; Logging
                 [org.clojure/tools.logging      "0.3.1"]
                 [ch.qos.logback/logback-classic "1.1.2"]
                 [org.slf4j/jul-to-slf4j         "1.7.7"]
                 [org.slf4j/jcl-over-slf4j       "1.7.7"]
                 [org.slf4j/log4j-over-slf4j     "1.7.7"]

                 [net.logstash.logback/logstash-logback-encoder "3.3"]]

  :uberimage {:base-image "mastodonc/basejava"
              :instructions ["EXPOSE 4001"]
              :cmd ["/bin/bash" "/start-eventlog"]
              :files {"start-eventlog" "docker/start-eventlog.sh"}
              :tag "mastodonc/kixi.eventlog"}

  :profiles {:dev {:dependencies [[lein-marginalia "0.8.0"]
                                  [org.clojure/tools.namespace "0.2.7"]]
                   :plugins [[com.palletops/uberimage "0.3.0"]]
                   :source-paths ["dev" "src"]
                   :resource-paths ["dev-resources" "resources"]}
             :uberjar {:main kixi.eventlog.main
                       :aot [kixi.eventlog.main]}})
