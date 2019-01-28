[![Build Status](https://travis-ci.org/magnetcoop/secret-storage.aws-ssm-ps.svg?branch=master)](https://travis-ci.org/magnetcoop/secret-storage.aws-ssm-ps)
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
Key initialization returns an `AWSParameterStore` record that can be used to perform the following operations:

* `get-key [record user-id]` Get secret key of the user specified by user-id.
* `put-key [record user-id encryption-key]` Put secret key assigned to the user specified by user-id.
* `delete-key [record user-id]` Delete secret key assigned to user-id.

## License

Copyright (c) Magnet S Coop 2018.

The source code for the library is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
