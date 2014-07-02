(ns random-graph-visualization.core)

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

(defn create-force-layout
  [width height]
  (-> js/d3 .-layout
      (.force)
      (.charge -120)
      (.linkDistance 30)
      (.size (array width height))))

(def margin {:top 50
             :bottom 50
             :left 50
             :right 50})

(defn create-svg
  [width height]
  (-> js/d3
      (.select "body")
      (.append "svg")
      (.attr "width" (+ (:left margin) width (:right margin))) 
      (.attr "height" (+ (:bottom margin) height (:top margin)))
      (.append "g")
      (.attr "transform"
             (str "translate(" (:left margin) "," (:top margin) ")"))))

(defn start-force-layout
  [force graph]
  (-> force
      (.nodes (aget graph "nodes"))
      (.links (aget graph "links"))
      .start))

(defn create-links
  [svg graph]
  (-> svg
      (.selectAll ".link")
      (.data (aget graph "links"))
      (.enter)
      (.append "line")
      (.attr "class" "link")
      (.style "stroke-width" 1)))

(defn create-nodes
  [svg force graph]
  (-> svg
      (.selectAll ".node")
      (.data (aget graph "nodes"))
      (.enter)
      (.append "circle")
      (.attr "class" "node")
      (.attr "r" 5)
      (.attr "data-n" #(:id %))
      (.style "fill" "cyan")
      (.call (aget force "drag"))))

(defn on-tick-handler
  [links nodes]
  (fn []
    (-> links
        (.attr "x1" #(-> % .-source .-x))
        (.attr "y1" #(-> % .-source .-y))
        (.attr "x2" #(-> % .-target .-x))
        (.attr "y2" #(-> % .-target .-y)))
    (-> nodes
        (.attr "cx" #(aget % "x"))
        (.attr "cy" #(aget % "y")))))

(defn render-graph
  [force svg graph]
  (let [json-graph (clj->js graph)]
    (start-force-layout force json-graph)
    (let [links (create-links svg json-graph)
          nodes (create-nodes svg force json-graph)]
      (.on force "tick" (on-tick-handler links nodes)))))

(let [width 650
      height 500]
  (render-graph
   (create-force-layout width height)
   (create-svg width height)
   test-graph))
