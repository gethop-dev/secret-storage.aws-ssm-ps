;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/

(ns magnet.secrets-storage.aws-ssm-ps
  (:require
   [amazonica.aws.simplesystemsmanagement :as ssm]
   [amazonica.core]
   [clojure.spec.alpha :as s]
   [magnet.secrets-storage.core :as core]
   [integrant.core :as ig]))

(s/def ::aws-kms-key string?)
(s/def ::user-keys-path string?)
(s/def ::AWSConfig (s/keys :req [::aws-kms-key ::user-keys-path]))

(defn- get-user-key-path
  "Build the path in Parameter Store for `user-id` user key"
  [config user-id]
  (format (:user-keys-path config) user-id))

(s/def ::user-id (s/or :string string? :uuid uuid?))
(s/def ::get-user-key-path-args (s/cat :config ::AWSConfig :user-id ::user-id))
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

(s/def ::get-crypt-key-args (s/cat :config ::AWSConfig :user-id ::user-id))
(s/def ::get-crypt-key-ret ::core/crypt-key)
(s/fdef get-crypt-key
  :args ::get-crypt-key-args
  :ret  ::get-crypt-key-ret)

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

(s/def ::put-crypt-key-args (s/cat :config ::AWSConfig :user-id ::user-id :crypt-key ::core/crypt-key))
(s/def ::put-crypt-key-ret (s/map-of keyword? int?)) ; Returns a map like {:version nn}
(s/fdef put-crypt-key
  :args ::put-crypt-key-args
  :ret  ::put-crypt-key-ret)

(defn- delete-crypt-key
  "Delete encryption key for user `user-id` from Parameter Store."
  [config user-id]
  (ssm/delete-parameter {:name (get-user-key-path config user-id)}))

(s/def ::delete-crypt-key-args (s/cat :config ::AWSConfig :user-id ::user-id))
(s/def ::delete-crypt-key-ret boolean?)
(s/fdef delete-crypt-key
  :args ::delete-crypt-key-args
  :ret  ::delete-crypt-key-ret)

(defrecord AWSParameterStore [config]
  core/UserEncryptionKeyStore
  (get-key [{:keys [config]} user-id]
    (get-crypt-key config user-id))
  (put-key [{:keys [config]} user-id encryption-key]
    (put-crypt-key config user-id encryption-key))
  (delete-key [{:keys [config]} user-id]
    (delete-crypt-key config user-id)))

(defmethod ig/init-key :magnet.secrets-storage/aws-ssm-ps [_ config]
  (->AWSParameterStore config))
