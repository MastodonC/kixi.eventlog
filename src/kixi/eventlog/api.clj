(ns kixi.eventlog.api
  (:require [liberator.core :refer (defresource)]))

(defresource index-resource
  :allowed-methods #{:post :get}
  :available-media-types ["application/json"]
  :known-content-type? ["application/json"]
  :handle-created "{\"status\": \"CREATED\"}")
