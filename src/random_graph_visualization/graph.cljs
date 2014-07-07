(ns random-graph-visualization.graph)

(defn graph-gnp
  [n p]
  (let [node-range (range n)
        edges (filter #(not (nil? %))
                          (for [u node-range v node-range]
                            (when (<= (rand) p)
                              {:source u :target v})))
        nodes (for [n node-range]
                {:id n})]
    {:nodes nodes
     :links edges}))

