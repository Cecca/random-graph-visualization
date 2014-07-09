(ns random-graph-visualization.render)

(defn force-layout
  [graph w h]
  (-> js/d3 .-layout
      (.force)
      (.charge -80)
      (.linkDistance 30)
      (.size (array w h))
      (.nodes (aget graph "nodes"))
      (.links (aget graph "links"))
      .start))

(def margin {:top 50
             :bottom 50
             :left 50
             :right 50})

(defn create-svg
  [width height]
  (-> js/d3
      (.select "#svg-container")
      (.append "svg")
      (.attr "width" width) 
      (.attr "height" height)
      (.append "g")
      (.attr "id" "drawing-area")
      #_(.attr "transform"
             (str "translate(" (/ width 2) "," (/ height 2) ")"))))

(defn create-links
  [svg graph]
  (-> svg
      (.selectAll ".link")
      (.remove))
  (let [links (-> svg
                  (.selectAll ".link")
                  (.data (aget graph "links")))]
    (-> links
        (identity))
    (-> links
        (.enter)
        (.append "line")
        (.attr "class" "link")
        (.style "stroke-width" 1))
    #_(-> links
        (.exit)
        (.remove))))

(defn create-nodes
  [svg force graph]
  (-> svg
      (.selectAll ".node")
      (.remove))
  (let [nodes (-> svg
                  (.selectAll ".node")
                  (.data (aget graph "nodes")))]
    (-> nodes
        (identity))
    (-> nodes
        (.enter)
        (.append "circle")
        (.attr "class" "node")
        (.attr "r" 5)
        (.attr "data-n" #(:id %))
        (.style "fill" "cyan")
        (.call (aget force "drag")))
    #_(-> nodes
        (.exit)
        (.remove))))

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

;; Since we are using om, I guess this is kind of an anti-pattern.
;; However, putting the current force layout in the global state would
;; trigger an endless rerendering cycle. So, if someone has a better
;; idea, please email me :-)
(def cur-force (atom nil))

(defn assoc-positions
  [graph]
  (if @cur-force
    (do
      (println (.nodes @cur-force))
      (let [{:keys [nodes links]} graph
            update-fun (fn [n force-n])
            updated-nodes (map merge nodes
                               (concat
                                (js->clj (.nodes @cur-force))
                                (repeat {})))]
        {:nodes updated-nodes
         :links links}))
    graph))

(defn render-graph
  [svg graph]
  (let [json-graph (clj->js (assoc-positions graph))
        force (force-layout json-graph 1000 320)
        links (create-links svg json-graph)
        nodes (create-nodes svg force json-graph)]
    (.on force "tick" (on-tick-handler links nodes))
    (reset! cur-force force)))

