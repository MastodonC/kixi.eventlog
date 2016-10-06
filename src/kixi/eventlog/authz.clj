(ns kixi.eventlog.authz
  (:require [buddy.core.keys :as ks]
            [buddy.sign.jwt :as jwt]
            [clojure.java.io :as io]
            [clj-http.client :as client]
            [clj-time.core :as t]
            [clj-time.coerce :as tc]
            [clojure.tools.logging :as log]))

(defn wrap-authentication
  [auth handler]
  (fn [request]
    (let [response (client/get (:heimdall auth))
          token (:auth-token (:token-pair (:body response)))
          unsigned (when token
                     (try (jwt/unsign token
                                      (ks/public-key (io/resource (:public-key auth)))
                                      {:alg :rs256  :now (tc/to-long (t/now))})
                          (catch Exception _ (log/debug "Unsign of token failed"))))]
      (if (and token unsigned)
        (handler (assoc request :user unsigned))
        (do (log/warn "Unauthenticated") {:status 401 :body "Unauthenticated"})))))

(defn maybe-wrap-authentication
  [auth]
  (log/info "maybe-wrap-auth" auth)
  (if auth
    (partial wrap-authentication auth)
    identity))
