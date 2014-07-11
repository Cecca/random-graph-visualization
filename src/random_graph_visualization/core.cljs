(ns random-graph-visualization.core
  (:require [random-graph-visualization.graph :refer [graph-gnp
                                                      poisson-graph
                                                      poisson-components]]
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

(defn log-slider
  [minp maxp minv maxv pos]
  (if (zero? pos)
    0
    (let [log-minv (.log js/Math minv)
          log-maxv (.log js/Math maxv)
          scale (/ (- log-maxv log-minv)
                   (- maxp minp))]
      (.exp js/Math (+ log-minv (* scale (- pos minp)))))))

(defn log-position
  [minp maxp minv maxv value]
  (let [log-minv (.log js/Math minv)
        log-maxv (.log js/Math maxv)
        scale (/ (- log-maxv log-minv)
                 (- maxp minp))]
    (/ (- (.log js/Math value) log-minv)
       (+ scale minp))))

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
                     :graph (poisson-components nodes deg)}))))

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
               
               #_(dom/div
                 #js {:className "input-div"})
               (dom/label #js {:for "degree-slider"}
                          (str "Average degree: " (:avg-deg state)))
               (dom/input #js {:type "range"
                               :id "degree-slider"
                               :min 0
                               :max 100
                               :step 0.001
                               :onMouseUp #(input-state-updater
                                            state
                                            (:num-nodes @state)
                                            (log-slider
                                             0
                                             100
                                             0.001
                                             (:num-nodes @state)
                                             (get-input-value "degree-slider")))})))))

(om/root controls-widget app-state
         {:target (. js/document (getElementById "controls"))})

(create-svg
 (.-clientWidth (. js/document (getElementById "svg-container")))
 (.-innerHeight js/window)
 #_(.-clientHeight (. js/document (getElementById "svg-container"))))

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
      (render-graph
       (.select js/d3 "#drawing-area")
       (:graph state)))))

(om/root visualization-widget app-state
         {:target (. js/document (getElementById "visualization"))
          :tx-listen #(println "State transition")})
