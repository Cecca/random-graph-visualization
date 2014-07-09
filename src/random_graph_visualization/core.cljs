(ns random-graph-visualization.core
  (:require [random-graph-visualization.graph :refer [graph-gnp poisson-graph]]
            [random-graph-visualization.render :refer [render-graph
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

(def app-state (atom {:graph {:nodes [] :links []}
                      :avg-deg 0.0
                      :num-nodes 0}))

(defn get-input-value
  [id]
  (.-value (. js/document (getElementById id))))

(defn input-state-updater
  [state nodes deg]
  (om/transact! state []
                (fn [old-state]
                  (if (and (= nodes (:num-nodes old-state))
                           (= deg (:avg-deg old-state)))
                    old-state
                    {:avg-deg deg
                     :num-nodes nodes
                     :graph (poisson-graph nodes deg)}))))

(defn controls-widget
  [state owner]
  (reify
    om/IRender
    (render [this]
      (dom/fieldset nil
               (dom/div
                #js {:className "input-div"}
                (dom/label #js {:for "nodes-input"} "Nodes")
                (dom/input #js {:type "number"
                                :id "nodes-input"
                                :name "nodes"
                                :min 0
                                :onChange #(input-state-updater
                                            state
                                            (get-input-value "nodes-input")
                                            (:avg-deg @state))}
                           nil))
               
               (dom/div
                #js {:className "input-div"}
                (dom/label #js {:for "degree-input"} "Average degree")
                (dom/input #js {:type "number"
                                :id "degree-input"
                                :name "average-degree"
                                :min 0}
                           nil))
               (dom/input #js {:type "range"
                               :id "degree-slider"
                               :min 0
                               :max (:num-nodes state)
                               :step 0.001
                               :onMouseUp #(println (get-input-value "degree-slider"))})
               (dom/button #js {:type "button"
                               :onClick
                               #(input-state-updater
                                 state
                                 (get-input-value "nodes-input")
                                 (get-input-value "degree-input"))}
                          "Render!")))))

(om/root controls-widget app-state
         {:target (. js/document (getElementById "controls"))})

(create-svg (.-innerWidth js/window) (.-innerHeight js/window))

(defn visualization-widget
  [state owner]
  (reify
    om/IWillMount
    (will-mount [this]
      (render-graph
       (.select js/d3 "#drawing-area")
       (:graph state)))
    om/IRender
    (render [this]
      (dom/div nil nil))
    om/IDidUpdate
    (did-update [this prev-props prev-state]
      (println "Updating graph")
      (render-graph
       (.select js/d3 "#drawing-area")
       (:graph state)))))

(om/root visualization-widget app-state
         {:target (. js/document (getElementById "visualization"))
          :tx-listen #(println "State transition")})
