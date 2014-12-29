(ns kixi.eventlog.api
  (:require [liberator.core :refer (defresource)]
            [clojure.tools.logging :as log]))

(defn malformed? [ctx]
  (if-let [body (get-in ctx [:request :body])]
    [false (assoc ctx ::body body)]
    true))

(defn handle-created [publish-fn ctx]
  (publish-fn (::body ctx)))

(defresource index-resource [publish-fn]
  :allowed-methods #{:post :get}
  :available-media-types ["application/json"]
  :known-content-type? ["application/json"]
  :malformed? malformed?
  :handle-created (partial handle-created publish-fn))
