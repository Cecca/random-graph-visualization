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

(defn state-logger
  [name state-map]
  (println name "=================\n"
           "Update path:" (:path state-map) "\n"
           "Old value:" (:old-value state-map) "\n"
           "New value:" (:new-value state-map)))

(def app-state (atom {:graph (poisson-graph 100 1)
                      :avg-deg 0.0
                      :num-nodes 100}))

(defn get-input-value
  [id]
  (.-value (. js/document (getElementById id))))

(defn input-state-updater
  [state nodes deg]
  (om/transact! state []
                (fn [old-state]
                  {:avg-deg deg
                   :num-nodes nodes
                   :graph (poisson-graph nodes deg)})))

(defn controls-widget
  [state owner]
  (reify
    om/IRender
    (render [this]
      (dom/div nil
               (dom/input #js {:type "number"
                               :id "nodes-input"
                               :name "nodes"
                               :min 0}
                          nil)
               (dom/input #js {:type "number"
                               :id "degree-input"
                               :name "average-degree"
                               :min 0}
                          nil)
               (dom/input #js {:type "button"
                               :onClick
                               #(input-state-updater
                                 state
                                 (get-input-value "nodes-input")
                                 (get-input-value "degree-input"))}
                          nil)))))

(om/root controls-widget app-state
         {:target (. js/document (getElementById "controls"))
          :tx-listen #(state-logger "filter-view" %)})

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

(comment
  (om/root visualization-widget app-state
           {:target (. js/document (getElementById "visualization"))}))
