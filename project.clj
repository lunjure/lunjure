(defproject lunjure "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [hiccup "1.0.0-RC3"]
                 [aleph "0.2.1-beta2"]
                 [ring/ring-core "1.1.0"]
                 [lein-swank "1.4.4"]]
  :plugins [[lein-cljsbuild "0.1.8"]]
  :main lunjure.core)
