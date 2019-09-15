;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/

(ns magnet.secrets-storage.util-test
  (:require [clojure.spec.test.alpha :as stest]
            [clojure.test :refer :all]
            [magnet.secrets-storage.util :as util]))


(defn enable-instrumentation [f]
  (-> (stest/enumerate-namespace 'magnet.secrets-storage.util) stest/instrument)
  (f))

(use-fixtures :once enable-instrumentation)

(def sample-encoded-base64 "UHN0ISBJJ20gYSBuaW5qYSE=")

(deftest util-test
  (is (=
       (util/encode-base64
        (util/decode-base64 sample-encoded-base64))
       sample-encoded-base64)
      "encoding is symmetric"))
