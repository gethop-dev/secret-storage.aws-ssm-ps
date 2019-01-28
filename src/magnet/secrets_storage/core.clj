;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/

(ns magnet.secrets-storage.core
  (:require
   [clojure.spec.alpha :as s]
   [magnet.secrets-storage.util :refer [encode-base64 decode-base64 base64?]]))

(s/def ::crypt-key bytes?)
(s/def ::serialized-crypt-key base64?)

(defn serialize [encryption-key]
  (encode-base64 encryption-key))

(s/fdef serialize
  :args #(s/cat :encryption-key ::crypt-key)
  :ret ::serialized-crypt-key)

(defn deserialize [encryption-key]
  (decode-base64 encryption-key))

(s/fdef deserialize
  :args #(s/cat :encryption-key ::serialized-crypt-key)
  :ret ::crypt-key)

(defprotocol UserEncryptionKeyStore
  "Abstraction for managing encryption keys used for PII encryption/decryption"
  (get-key [this user-id] "Get encryption key of the user specified by user-id")
  (put-key [this user-id encryption-key] "Put encryption key assigned to the user specified by user-id")
  (delete-key [this user-id] "Delete encryption key assigned to user-id"))
