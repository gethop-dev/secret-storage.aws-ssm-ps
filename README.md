[![Build Status](https://api.travis-ci.com/magnetcoop/secret-storage.aws-ssm-ps.svg?branch=master)](https://travis-ci.com/magnetcoop/secret-storage.aws-ssm-ps)
[![Clojars Project](https://img.shields.io/clojars/v/magnet/secret-storage.aws-ssm-ps.svg)](https://clojars.org/magnet/secret-storage.aws-ssm-ps)

# Duct Secret Storage

A [Duct](https://github.com/duct-framework/duct) library that provides [Integrant](https://github.com/weavejester/integrant) keys for managing user's secret keys stored in AWS System Manager Parameter Store.

## Installation

[![Clojars Project](https://clojars.org/magnet/secret-storage.aws-ssm-ps/latest-version.svg)](https://clojars.org/magnet/secret-storage.aws-ssm-ps)

## Usage

This library provides a single Integrant key, `:magnet.secrets-storage/aws-ssm-ps`, that expects the following keys:

* `:aws-kms-key`: Parameter Store uses AWS KMS to encrypt and decrypt the stored parameters. Here you specify which KMS key should be used for that purpose. You can give the entire key or use the alias in this format: `alias/youralias`.
* `:user-keys-path`: The path to where the keys should be saved. The path string should contain the wildcard `%s` that will be replaced with the proper `user-id` in each case.

Example usage:

``` edn
  :magnet.secrets-storage/aws-ssm-ps
  {:aws-kms-key "alias/hydrogen"
   :user-keys-path "/hydrogen/user-keys/%s"}
```
Key initialization returns an `AWSParameterStore` record that can be used to perform the operations described below:

``` clojure
user> (require '[magnet.secrets-storage.core :as secrets-storage]
               '[magnet.secrets-storage.aws-ssm-ps]
               '[integrant.core :as ig])
nil
user> (def config {:aws-kms-key (System/getenv "SSM_SP_AWS_KMS_KEY")
                   :user-keys-path (System/getenv "SSM_SP_USER_KEYS_PATH")})
#'user/config
user> (def aws-ssm-ps-boundary (ig/init-key :magnet.secrets-storage/aws-ssm-ps config))
#'user/aws-ssm-ps-boundary
```

#### Get the secret key of the user specified by user-id

If the user-id has a secret key stored in AWS SSM Parameter Store and we can retrieve it, `get-key` returns a map with `:success??` set to `true`, and `:key` to the array of bytes for the key.

``` clojure
user> (secrets-storage/get-key aws-ssm-ps-boundary "user-id-with-existing-key")
{:success? true,
 :key [-94, -9, -87, 125, -122, 49, -99, -58, -84, 51, 28, -62, 27, 20,
       61, -117, -34, 102, -117, -25, -17, -67, -107, 67, -26, -27, -40,
       -52, 80, 90, 3, 84]}

```

If the user-id doesn't have a secret key in AWS SSM Parameter Store, or there was any kind of problem trying to get the key, `get-key` returns a map with `:success?` set to `false` and `:error-details` with a map with additional details on the problem:

``` clojure
user> (secrets-storage/get-key aws-ssm-ps-boundary "user-id-with-nonexistent-key")
{:success? false,
 :error-details
 {:error-code "ParameterNotFound",
  :error-type "Client",
  :status-code 400,
  :request-id "13bffccc-32d6-4b94-be56-0c74388cba09",
  :service-name "AWSSimpleSystemsManagement",
  :message
  "null (Service: AWSSimpleSystemsManagement; Status Code: 400; Error Code: ParameterNotFound; Request ID: 13bffccc-32d6-4b94-be56-0c74388cba09)",
  :stack-trace "...."}}
```

#### Store the secret key assigned to the user specified by user-id

If the key (a byte array) to be associated with the user-id can be stored in AWS SSM Parameter Store, `put-key` returns a map with `:success?` set to true:

``` clojure
user> (def not-very-random-encryption-key (byte-array [1 2 3 4 5 6]))
#'user/not-very-random-encryption-key
user> (secrets-storage/put-key aws-ssm-ps-boundary "some-user-id" not-very-random-encryption-key)
{:success? true}
```

If it can be stored, then it returns a map with `:success?` set to `false` and `:error-details` with a map with additional details on the problem:

``` clojure
user> (secrets-storage/put-key aws-ssm-ps-boundary "some-user-id" not-very-random-encryption-key)
{:success? false,
 :error-details
 {:error-code "InvalidKeyId",
  :error-type "Client",
  :status-code 400,
  :request-id "dc99176f-365b-4aa4-8d2c-77c9b8332ac2",
  :service-name "AWSSimpleSystemsManagement",
  :message "Invalid keyId ....",
  :stack-trace  "...."}}
```

#### Delete secret key assigned to user-id

If the user-id has a secret key stored in AWS SSM Parameter Store and we can delete it, `delete-key` returns a map with `:success?` set to `true`:

``` clojure
user> (secrets-storage/delete-key aws-ssm-ps-boundary "user-id-with-existing-key")
{:success? true}
```

If the user doesn't have a secret key in AWS SSM Parameter Store, or there was any kind of problem trying to get the key, `get-key` returns a map with `:success?` set to `false` and `:error-details` with a map with additional details on the problem:

``` clojure
user> (secrets-storage/delete-key aws-ssm-ps-boundary "user-id-with-nonexistent-key")
{:success? false,
 :error-details
 {:error-code "ParameterNotFound",
  :error-type "Client",
  :status-code 400,
  :request-id "13bffccc-32d6-4b94-be56-0c74388cba09",
  :service-name "AWSSimpleSystemsManagement",
  :message
  "null (Service: AWSSimpleSystemsManagement; Status Code: 400; Error Code: ParameterNotFound; Request ID: 13bffccc-32d6-4b94-be56-0c74388cba09)",
  :stack-trace "...."}}
```

## License

Copyright (c) 2018, 2019 Magnet S Coop.

The source code for the library is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
