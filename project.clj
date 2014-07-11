(defproject random-graph-visualization "0.1.0-SNAPSHOT"
  :description "Clujurescript visualization of a random graph"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2261"]
                 [om "0.6.4"]]

  :plugins [[lein-cljsbuild "1.0.3"]]

  :source-paths ["src"]

  :cljsbuild { 
    :builds [{:id "random-graph-visualization"
              :source-paths ["src"]
              :compiler {
                :output-to "random_graph_visualization.js"
                :output-dir "out"
                :optimizations :none
                :source-map true}}]})
