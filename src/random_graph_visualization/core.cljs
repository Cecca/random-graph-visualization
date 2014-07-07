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
                      :num-nodes 100}))

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
    om/IWillMount
    (will-mount [_]
      nil)
    om/IRenderState
    (render-state [this {:keys [val]}]
      (dom/p nil
             (dom/p nil (str "Average degree: " (:avg-deg data)))
             (slider :val owner
                     :step nil ; continuous 
                     :max (dec (:num-nodes data)))))
    om/IDidUpdate
    (did-update [this prev-props prev-state]
      (om/update! data [:avg-deg] (:val prev-state)))))

(om/root slider-widget app-state
  {:target (. js/document (getElementById "slider"))})

(defn visualization-widget
  [state owner]
  (reify
    om/IWillMount
    (will-mount [this]
      nil)
    om/IRender
    (render [this]
      (dom/p nil "ciao"))
    om/IDidUpdate
    (did-update [this prev-props prev-state]
      nil)))

(om/root visualization-widget app-state
  {:target (. js/document (getElementById "visualization"))})
