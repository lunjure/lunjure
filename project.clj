(defproject lunjure "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [hiccup "1.0.0-RC3"]
                 [aleph "0.2.1-beta2"]
                 [ring/ring-core "1.1.0"]
                 [lein-swank "1.4.4"]
                 [commons-codec "1.5" :exclusions [commons-logging]];Base64
                 ]
  :main lunjure.core
  :plugins [[lein-cljsbuild "0.1.8"]]
  :cljsbuild {:crossovers []
              :builds [{:source-path "src-cljs/"
                        :compiler
                        {:output-to "resources/public/lunjure.js"
                         :pretty-print true
                         :optimizations :whitespace}}]})
