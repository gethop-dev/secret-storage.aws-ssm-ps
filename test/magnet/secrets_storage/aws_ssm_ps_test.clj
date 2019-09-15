;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/

(ns  magnet.secrets-storage.aws-ssm-ps-test
  (:require [clojure.spec.test.alpha :as stest]
            [clojure.test :refer :all]
            [magnet.secrets-storage.core :as core]
            [magnet.secrets-storage.util :refer [encode-base64]]
            [magnet.secrets-storage.aws-ssm-ps]
            [integrant.core :as ig])
  (:import [magnet.secrets_storage.aws_ssm_ps AWSParameterStore]
           [com.amazonaws.services.simplesystemsmanagement.model ParameterNotFoundException]
           [java.util UUID]))

(defn enable-instrumentation [f]
  (-> (stest/enumerate-namespace 'magnet.secrets-storage.aws-ssm-ps) stest/instrument)
  (f))

(use-fixtures :once enable-instrumentation)

(def config {:aws-kms-key (System/getenv "SSM_SP_AWS_KMS_KEY")
             :user-keys-path (System/getenv "SSM_SP_USER_KEYS_PATH")})

(def enc-key-owner (System/getenv "SSM_SP_TESTS_ENC_KEY_OWNER"))

(deftest protocol-test
  (let [aws-ssm-ps-boundary (ig/init-key :magnet.secrets-storage/aws-ssm-ps config)]
    (is
     (= (class aws-ssm-ps-boundary)
        AWSParameterStore))))

(deftest ^:integration aws-ssm-ps-test
  (let [aws-ssm-ps-boundary (ig/init-key :magnet.secrets-storage/aws-ssm-ps config)]
    (is
     (core/get-key aws-ssm-ps-boundary enc-key-owner))
    (is
     (encode-base64 (core/get-key aws-ssm-ps-boundary enc-key-owner))
     "It should be possible to encode key to base64 as it was mandatory format to even exist in AWS SSM PS")
    (is
     (thrown?
      ParameterNotFoundException
      (core/get-key aws-ssm-ps-boundary (str (UUID/randomUUID))))
     "Getting key of a user that doesn't exist should fail.")
    (let [new-user-id (str (UUID/randomUUID))
          secret-length 32
          secret-1 (byte-array (repeatedly secret-length #(rand-int 256)))
          secret-2 (byte-array (repeatedly secret-length #(rand-int 256)))]
      (is
       (= (core/put-key aws-ssm-ps-boundary
                        new-user-id
                        secret-1)
          {:version 1})
       "It should be possible to create a new user with new key.")
      (is
       (= (core/put-key aws-ssm-ps-boundary
                        new-user-id
                        secret-2)
          {:version 2})
       "It should be possible to put new version of a key.")
      (is
       (= (core/delete-key aws-ssm-ps-boundary
                           new-user-id)
          {})
       "It should be possible to delete a key.")
      (is
       (thrown? ParameterNotFoundException
                (core/get-key aws-ssm-ps-boundary
                              new-user-id))
       "After a key gets deleted it should be impossible to get anything from that user.")
      (is
       (= (core/put-key aws-ssm-ps-boundary
                        new-user-id
                        secret-1)
          {:version 1})
       "If the key gets deleted, it should be possible to start all over again."))))
