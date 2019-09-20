;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/

(ns magnet.secrets-storage.aws-ssm-ps
  (:require [amazonica.aws.simplesystemsmanagement :as ssm]
            [amazonica.core :refer [ex->map]]
            [clojure.spec.alpha :as s]
            [integrant.core :as ig]
            [magnet.secrets-storage.core :as core]))

(s/def ::aws-kms-key string?)
(s/def ::user-keys-path string?)
(s/def ::AWSConfig (s/keys :req-un [::aws-kms-key ::user-keys-path]))

(defn- get-user-key-path
  "Build the path in Parameter Store for `user-id` user key"
  [config user-id]
  (format (:user-keys-path config) user-id))

(s/def ::get-user-key-path-args (s/cat :config ::AWSConfig :user-id ::core/user-id))
(s/def ::get-user-key-path-ret string?)
(s/fdef get-user-key-path
  :args ::get-user-key-path-args
  :ret  ::get-user-key-path-ret)

(defn- get-crypt-key
  "Get encryption key for user `user-id` from Parameter Store."
  [config user-id]
  (->
   (ssm/get-parameter
    {:name (get-user-key-path config user-id)
     :with-decryption true})
   (get-in [:parameter :value])
   (core/deserialize)))

(s/fdef get-crypt-key
  :args ::core/get-key-args
  :ret  ::core/get-key-ret)

(defn- put-crypt-key
  "Put encryption key `crypt-key` for user `user-id` in Parameter Store."
  [config user-id crypt-key]
  {:pre [(and (s/valid? ::user-id user-id) (s/valid? ::core/crypt-key crypt-key))]}
  (ssm/put-parameter
   {:name (get-user-key-path config user-id)
    :type "SecureString"
    :overwrite true
    :key-id (:aws-kms-key config)
    :value (core/serialize crypt-key)}))

(s/fdef put-crypt-key
  :args ::core/put-key-args
  :ret  ::core/put-key-ret)

(defn- delete-crypt-key
  "Delete encryption key for user `user-id` from Parameter Store."
  [config user-id]
  (ssm/delete-parameter {:name (get-user-key-path config user-id)}))

(s/fdef delete-crypt-key
  :args ::core/delete-key-args
  :ret  ::core/delete-key-ret)

(defrecord AWSParameterStore [config]
  core/UserEncryptionKeyStore
  (get-key [this user-id]
    (get-crypt-key this user-id))
  (put-key [this user-id encryption-key]
    (put-crypt-key this user-id encryption-key))
  (delete-key [this user-id]
    (delete-crypt-key this user-id)))

(defmethod ig/init-key :magnet.secrets-storage/aws-ssm-ps [_ config]
  (->AWSParameterStore config))
