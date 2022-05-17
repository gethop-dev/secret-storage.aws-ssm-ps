;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/

(ns  dev.gethop.secrets-storage.aws-ssm-ps-test
  (:require [clojure.spec.test.alpha :as stest]
            [clojure.test :refer :all]
            [dev.gethop.secrets-storage.aws-ssm-ps]
            [dev.gethop.secrets-storage.core :as core]
            [dev.gethop.secrets-storage.util :refer [encode-base64]]
            [integrant.core :as ig])
  (:import [dev.gethop.secrets_storage.aws_ssm_ps AWSParameterStore]
           [java.util UUID]))

(defn enable-instrumentation [f]
  (-> (stest/enumerate-namespace 'dev.gethop.secrets-storage.aws-ssm-ps) stest/instrument)
  (f))

(use-fixtures :once enable-instrumentation)

(def config {:aws-kms-key "alias/Hydrogen"
             :user-keys-path "/hydrogen/user-keys/unit-tests/%s"})

(def enc-key-owner "0ef87379-b534-4004-b08f-36e429856c63")

(deftest protocol-test
  (let [aws-ssm-ps-boundary (ig/init-key :dev.gethop.secrets-storage/aws-ssm-ps config)]
    (is
     (= (class aws-ssm-ps-boundary)
        AWSParameterStore))))

(deftest ^:integration aws-ssm-ps-test
  (let [aws-ssm-ps-boundary (ig/init-key :dev.gethop.secrets-storage/aws-ssm-ps config)]
    (let [result (core/get-key aws-ssm-ps-boundary enc-key-owner)]
      (is
       (:success? result)
       "It should be possible to get an existing key")
      (is
       (encode-base64 (:key result))
       "It should be possible to encode key to base64 as it was mandatory format to even exist in AWS SSM PS"))
    (let [result (core/get-key aws-ssm-ps-boundary (str (UUID/randomUUID)))]
      (is
       (and (= false (:success? result))
            (= "ParameterNotFound" (get-in result [:error-details :error-code])))
       "Getting key of a user that doesn't exist should fail."))
    (let [new-user-id (str (UUID/randomUUID))
          secret-length 32
          secret-1 (byte-array (repeatedly secret-length #(rand-int 256)))
          secret-2 (byte-array (repeatedly secret-length #(rand-int 256)))]
      (is
       (= (core/put-key aws-ssm-ps-boundary new-user-id secret-1)
          {:success? true})
       "It should be possible to create a new user with new key.")
      (is
       (= (core/put-key aws-ssm-ps-boundary new-user-id secret-2)
          {:success? true})
       "It should be possible to update (overwrite) a key.")
      (is
       (= (core/delete-key aws-ssm-ps-boundary new-user-id)
          {:success? true})
       "It should be possible to delete a key.")
      (let [result (core/get-key aws-ssm-ps-boundary new-user-id)]
        (is
         (and (= false (:success? result))
              (= "ParameterNotFound" (get-in result [:error-details :error-code])))
         "After a key gets deleted it should be impossible to get anything from that user.")))))
