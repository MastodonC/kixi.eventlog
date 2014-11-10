(ns kixi.eventlog.api
  (:require [liberator.core :refer (defresource)]))

(defn handle-created [publish-fn ctx]
  (publish-fn (get-in ctx [:request :body])))

(defresource index-resource [producer]
  :allowed-methods #{:post :get}
  :available-media-types ["application/json"]
  :known-content-type? ["application/json"]
  :handle-created (partial handle-created producer))
