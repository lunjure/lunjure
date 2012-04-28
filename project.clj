(defproject lunjure "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [org.clojure/data.json "0.1.2"]
                 [hiccup "1.0.0-RC3"]
                 [aleph "0.2.1-beta2"]
                 [ring/ring-core "1.1.0"]
                 [clj-redis "0.0.13-SNAPSHOT"]
                 [lein-swank "1.4.4"]
                 [compojure "1.0.2"]
                 [clj-foursquare "0.1-SNAPSHOT"]
                 [commons-codec "1.5" :exclusions [commons-logging]] ;Base64
                 [lamina "0.4.1-beta2"]
                 ]
  :main lunjure.core
  :plugins [[lein-cljsbuild "0.1.8"]]
  :repositories {"local" ~(str (.toURI (java.io.File. "maven-repo")))}
  :cljsbuild {:crossovers []
              :builds [{:source-path "src-cljs/"
                        :compiler
                        {:output-to "resources/public/lunjure.js"
                         :pretty-print true
                         :optimizations :whitespace}}]})
