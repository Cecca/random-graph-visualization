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

(defn graph->adj
  [graph]
  (let [rev-edges (map (fn [x] {:source (:target x)
                               :target (:source x)})
                       (:links graph))
        complete-edges (concat rev-edges (:links graph))]
    (into {}
          (map (fn [[k v]] [k (into #{} (map :target v))])
               (group-by :source complete-edges)))))

(defn bfs
  [v adj]
  (loop [cc #{}
        queue #queue [v]]
    (if (empty? queue)
      cc
      (let [nxt (clojure.set/difference (get adj (peek queue)) cc)]
        (recur (into cc nxt)
               (into (pop queue) nxt))))))

(defn connected-components
  [graph]
  (let [adj (graph->adj graph)]
    (loop [remaining (into #{} (map :id (:nodes graph)))
           cc {}
           i 0]
      (if (empty? remaining)
        cc
        (let [valid (into #{} (drop-while #(nil? (get adj %)) remaining))
              c (bfs (first valid) adj)]
          (recur (clojure.set/difference valid c)
                 (assoc cc i c)
                 (inc i)))))))

(defn poisson-components
  [n c]
  (let [graph (poisson-graph n c)
        cc (connected-components graph)
        cc-map (apply merge
                      (flatten (for [[i c] cc]
                                 (map (fn [x] {x i}) c))))]
    (assoc graph :nodes (for [v (:nodes graph)]
                         (assoc v :component (get cc-map (:id v)))))))
