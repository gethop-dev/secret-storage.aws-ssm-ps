(defproject dev.gethop/secret-storage.aws-ssm-ps "0.5.2-SNAPSHOT"
  :description "Store secret values in AWS Systems Manager Parameter Store"
  :url "https://github.com/gethop-dev/secret-storage.aws-ssm-ps"
  :license {:name "Mozilla Public Licence 2.0"
            :url "https://www.mozilla.org/en-US/MPL/2.0/"}
  :min-lein-version "2.9.8"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [amazonica "0.3.143" :exclusions [com.amazonaws/aws-java-sdk
                                                   com.amazonaws/amazon-kinesis-client
                                                   com.amazonaws/dynamodb-streams-kinesis-adapter]]
                 [com.amazonaws/aws-java-sdk-core "1.11.586"]
                 [com.amazonaws/aws-java-sdk-s3 "1.11.586"]
                 [com.amazonaws/aws-java-sdk-ssm "1.11.586"]
                 [integrant "0.7.0"]]
  :deploy-repositories [["snapshots" {:url "https://clojars.org/repo"
                                      :username :env/CLOJARS_USERNAME
                                      :password :env/CLOJARS_PASSWORD
                                      :sign-releases false}]
                        ["releases"  {:url "https://clojars.org/repo"
                                      :username :env/CLOJARS_USERNAME
                                      :password :env/CLOJARS_PASSWORD
                                      :sign-releases false}]]
  :profiles
  {:dev [:project/dev :profiles/dev]
   :repl {:repl-options {:host "0.0.0.0"
                         :port 4001}}
   :profiles/dev {}
   :project/dev {:plugins [[jonase/eastwood "1.2.3"]
                           [lein-cljfmt "0.8.0"]]
                 :eastwood {:linters [:all]
                            :source-paths ["src"]
                            :test-paths ["test"]
                            :exclude-linters [:reflection
                                              :unused-namespaces]
                            :debug [:progress :time]}}})
