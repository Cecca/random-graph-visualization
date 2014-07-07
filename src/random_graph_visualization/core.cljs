(ns random-graph-visualization.core
  (:require [random-graph-visualization.graph :refer [graph-gnp]]
            [random-graph-visualization.render :refer [render-graph
                                                       create-force-layout
                                                       create-svg]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [om.widget.slider :refer [slider]]))

(enable-console-print!)

(def app-state (atom {:graph {:nodes []
                              :links []}
                      :avg-deg 0.0
                      :num-nodes 0}))

(comment
  (def rnd-graph
    (graph-gnp 10 0.1))

  (let [width 800
        height 700]
    (render-graph
     (create-force-layout width height)
     (create-svg width height)
     rnd-graph)))

(defn slider-widget
  [data owner]
  (reify
    om/IRenderState
    (render-state [this {:keys [val]}]
      (dom/p nil
             (str "The value is " val)
             (slider :val owner)))))

(om/root slider-widget app-state
  {:target (. js/document (getElementById "slider"))})

