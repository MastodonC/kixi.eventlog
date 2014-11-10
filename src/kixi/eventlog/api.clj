(ns kixi.eventlog.api
  (:require [liberator.core :refer (defresource)]))

(defn handle-created [producer ctx]

  )
(defresource index-resource [producer]
  :allowed-methods #{:post :get}
  :available-media-types ["application/json"]
  :known-content-type? ["application/json"]
  :handle-created (partial handle-created producer))
