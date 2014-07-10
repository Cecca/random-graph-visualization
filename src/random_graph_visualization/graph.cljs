(ns random-graph-visualization.graph)

;; TODO: optimize by symmetrizing
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

(defn poisson-graph
  [n c]
  (graph-gnp n (/ c (dec n))))

(defn connected-components
  [graph]
  (let [rev-edges (map (fn [x] {:source (:target x)
                               :target (:source x)})
                       (:links graph))
        complete-edges (concat rev-edges (:links graph))
        adj (into {}
                  (map (fn [[k v]] [k (into #{} (map :target v))])
                       (group-by :source complete-edges)))]
    (loop [remaining (:nodes graph)
           cc {}
           i 0]
      (if (empty? remaining)
        cc))))
