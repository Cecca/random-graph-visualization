(ns random-graph-visualization.core
  (:require [random-graph-visualization.graph :refer [graph-gnp]]
            [random-graph-visualization.render :refer [render-graph
                                                       create-force-layout
                                                       create-svg]]))

(enable-console-print!)

(def app-state (atom {}))

(def rnd-graph
  (graph-gnp 100 0.01))

(println rnd-graph)

(let [width 800
      height 700]
  (render-graph
   (create-force-layout width height)
   (create-svg width height)
   rnd-graph))
