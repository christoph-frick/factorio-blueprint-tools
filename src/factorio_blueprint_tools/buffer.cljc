(ns factorio-blueprint-tools.buffer
  (:require [factorio-blueprint-tools.blueprint :as blueprint]
            [factorio-blueprint-tools.sizes :as sizes]
            [com.rpl.specter :as s]))

(def buffer-chest-slots 48)

(defn histogram
  [blueprint]
  (frequencies (s/select (conj blueprint/entities-path :name) blueprint)))

(defn next-request-filter
  [histogram entity-name]
  (assert (contains? histogram entity-name))
  (let [remaining (histogram entity-name)
        stack-size (sizes/stack-size entity-name)]
    (if (<= remaining stack-size)
      [(dissoc histogram entity-name) remaining]
      [(update histogram entity-name #(- % stack-size)) stack-size])))

(defn request-filter-from-histogram
  [histogram]
  (assert (seq histogram))
  (reduce (fn [[histogram result] entity-name]
            (let [[histogram cnt] (next-request-filter histogram entity-name)]
              [histogram (assoc result entity-name cnt)]))
          [histogram {}]
          (take buffer-chest-slots (keys histogram))))

(defn request-filters-from-histogram
  [histogram]
  (loop [histogram histogram
         result []]
    (let [[histogram r] (request-filter-from-histogram histogram)
          result (conj result r)]
      (if (seq histogram)
        (recur histogram result)
        result))))

(defn request-filter
    [name count]
    {:name name
     :count count})

(defn request-filters
  [filters]
  (mapv (fn [i [n c]]
          (assoc (request-filter n c)
                 :index i))
        (iterate inc 1)
        filters))

(defn buffer-chest
  [filters]
  {:name "logistic-chest-buffer"
   :request_filters (request-filters filters)})

(defn buffer-chest-blueprint
  [blueprint]
  (let [histogram (histogram blueprint)
        request-filters (request-filters-from-histogram histogram)
        buffer-chests (map (fn [{:keys [entity_number] :as entity}]
                             (assoc entity :position {:x (+ 0.5 entity_number) :y 0.5}))
                           (blueprint/fix-entity-numbers (map buffer-chest request-filters)))]
    (assoc-in {} blueprint/entities-get-in buffer-chests)))
