(ns random-graph-visualization.core
  (:require [random-graph-visualization.render :refer [render-graph
                                                       create-force-layout
                                                       create-svg]]))

(enable-console-print!)

(def app-state (atom {}))

(def test-graph
  {:nodes [{:id 0}
           {:id 1}
           {:id 2}
           {:id 3}
           {:id 4}]
   :links [{"source" 0 "target" 1}
           {"source" 0 "target" 2}
           {"source" 0 "target" 3}
           {"source" 0 "target" 4}]})

(let [width 650
      height 500]
  (render-graph
   (create-force-layout width height)
   (create-svg width height)
   test-graph))
