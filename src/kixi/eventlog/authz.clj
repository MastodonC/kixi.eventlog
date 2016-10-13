(ns kixi.eventlog.authz
  (:require [buddy.core.keys :as ks]
            [buddy.sign.jwt :as jwt]
            [clojure.java.io :as io]
            [clj-http.client :as client]
            [clj-time.core :as t]
            [clj-time.coerce :as tc]
            [clojure.tools.logging :as log]
            [clojure.data.json :as json]
            [ring.middleware.basic-authentication :as basic-auth]))

(defn authentication-fn
  "function used by basic auth middleware to decide whether the request should proceed"
  [auth]
  (fn [username password]
    (let [response (client/post (str (:heimdall auth) "/create-auth-token") {:form-params {:username username :password password} :content-type :json :throw-exceptions false})
          body (json/read-str (:body response) :key-fn keyword)
          token (:auth-token (:token-pair body))
          unsigned (when token
                     (try (jwt/unsign token
                                      (ks/public-key (io/resource (:public-key auth)))
                                      {:alg :rs256  :now (tc/to-long (t/now))})
                          (catch Exception e (log/info "Unsign of token failed" e))))]
      (if (and token unsigned)
        true
        (log/warn "Unauthenticated")))))

(defn maybe-wrap-authentication
  [authentication? auth]
  (fn [handler]
    (if authentication?
      (basic-auth/wrap-basic-authentication handler (authentication-fn auth))
      (fn [request]
        (handler request))))
  )
