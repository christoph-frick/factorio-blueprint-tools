(ns factorio-blueprint-tools.mirror
  (:require [com.rpl.specter :as s]
            [factorio-blueprint-tools.blueprint :as blueprint]))

; curved rails are tricky with their direction:
; a full circle looks like this (starting with at 01:30 o'clock):
; 3 0 5 2 7 4 1 6 

(def directions
  ; direction, axis to negate, direction to change; direction to change for curved rails
  {:vertically [:x {0 0, 1 7, 2 6, 3 5, 4 4, 5 3, 6 2, 7 1} {0 1, 1 0, 2 7, 3 6, 4 5, 5 4, 6 3, 7 2}]
   :horizontally [:y {0 4, 1 3, 2 2, 3 1, 4 0, 5 7, 6 6, 7 5} {0 5, 1 4, 2 3, 3 2, 4 1, 5 0, 6 7, 7 6}]})

(defn- mirror-direction
  [mapping direction]
  (mapping (or direction 0)))

(defn mirror
  [blueprint direction]
  (let [[axis dir-map dir-map-curved-rail] (get directions direction (:vertically directions))]
    (s/transform [:blueprint :entities s/ALL]
                 (fn [entity]
                   (-> entity
                       (update :direction (partial mirror-direction (if (= (:name entity) "curved-rail") dir-map-curved-rail dir-map)))
                       (update-in [:position axis] -)))
                 blueprint)))
