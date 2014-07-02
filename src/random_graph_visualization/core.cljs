(ns random-graph-visualization.core)

(enable-console-print!)

(def app-state (atom {}))

(def test-graph
  {:nodes [{:id 0}
           {:id 1}
           {:id 2}
           {:id 3}
           {:id 4}]
   :edges [{:source 0 :target 1}
           {:source 0 :target 2}
           {:source 0 :target 3}
           {:source 0 :target 4}]})

(defn create-force-layout
  [width height]
  (-> js/d3 .-layout
      (.force)
      (.charge -120)
      (.linkDistance 30)
      (.size (array width height))))

(defn create-svg
  [width height]
  (-> js/d3
      (.select "body")
      (.append "svg")
      (.attr "width" width) 
      (.attr "height" height)
      (.append "g")))

(defn start-force-layout
  [force graph]
  (-> force
      (.nodes (clj->js (:nodes graph)))
      (.links (clj->js (:edges graph)))
      .start))

(defn create-edges
  [svg graph]
  (-> svg
      (.selectAll "line.edge")
      (.data (clj->js (:edges graph)))
      (.enter)
      (.append "line")
      (.attr "class" "edge")
      (.style "stroke-width" 1)))

(defn create-nodes
  [svg force graph]
  (-> svg
      (.selectAll "circle.node")
      (.data (clj->js (:nodes graph)))
      (.enter)
      (.append "circle")
      (.attr "class" "node")
      (.attr "r" 5)
      (.attr "data-n" #(:id %))
      (.style "fill" "cyan")
      (.call (aget force "drag"))))

(defn on-tick-handler
  [edge node]
  (fn []
    (-> edge
        (.attr "x1" #(-> % .-source .-x))
        (.attr "y1" #(-> % .-source .-y))
        (.attr "x2" #(-> % .-target .-x))
        (.attr "y2" #(-> % .-target .-y)))
    (-> node
        (.attr "cx" #(aget % "x"))
        (.attr "cy" #(aget % "y")))))

(defn render-graph
  [force svg graph]
  (start-force-layout force graph)
  (let [edges (create-edges svg graph)
        nodes (create-nodes svg force graph)]
    (.on force "tick" (on-tick-handler edges nodes))))

(let [width 650
      height 500]
  (render-graph
   (create-force-layout width height)
   (create-svg width height)
   test-graph))
