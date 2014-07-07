(ns random-graph-visualization.core
  (:require [random-graph-visualization.graph :refer [graph-gnp poisson-graph]]
            [random-graph-visualization.render :refer [render-graph
                                                       create-force-layout
                                                       create-svg]]
            [cljs.core.async :as async :refer [<! >! timeout]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [om.widget.slider :refer [slider]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(enable-console-print!)

(def app-state (atom {:graph (poisson-graph 10 2)
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
      (go-loop []
        (om/update! data [:avg-deg] (:val (om/get-state owner)))
        (<! (timeout 2000))
        (recur)))
    om/IRenderState
    (render-state [this {:keys [val]}]
      (dom/p nil
             (dom/p nil (str "Average degree: " (:avg-deg data)))
             (slider :val owner
                     :step nil ; continuous 
                     :max (dec (:num-nodes data)))))))

(om/root slider-widget app-state
  {:target (. js/document (getElementById "slider"))})

(defn visualization-widget
  [state owner]
  (reify
    om/IWillMount
    (will-mount [this]
      (create-svg 800 700)
      (render-graph
       (create-force-layout 800 700)
       (.select js/d3 "#drawing-area")
       (:graph state)))
    om/IRender
    (render [this]
      (dom/div nil (str "Average degree is " (:avg-deg state))))
    om/IDidUpdate
    (did-update [this prev-props prev-state]
      (comment)
      (println "Updating graph")
      (render-graph
       (create-force-layout 800 700)
       (.select js/d3 "#drawing-area")
       (:graph prev-props)))))

(om/root visualization-widget app-state
  {:target (. js/document (getElementById "visualization"))})
