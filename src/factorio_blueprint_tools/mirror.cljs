(ns factorio-blueprint-tools.mirror
  (:require [com.rpl.specter :as s]
            [factorio-blueprint-tools.blueprint :as blueprint]))

(def directions
  ; direction, axis to negate, direction to change
  {:vertically [:x {0 0, 1 7, 2 6, 3 5, 4 4, 5 3, 6 2, 7 1}]
   :horizontally [:y {0 4, 1 3, 2 2, 3 1, 4 0, 5 7, 6 6, 7 5}]})

(defn- mirror-direction
  [mapping direction]
  (mapping (or direction 0)))

(defn mirror
  [blueprint direction]
  (let [[axis direction-mapping] (get directions direction (:vertically directions))]
    (s/transform [:blueprint :entities s/ALL]
                 (fn [entity]
                   (-> entity
                       (update :direction (partial mirror-direction direction-mapping))
                       (update-in [:position axis] -)))
                 blueprint)))
