(ns factorio-blueprint-tools.mirror
  (:require [com.rpl.specter :as s]
            [factorio-blueprint-tools.blueprint :as blueprint]))

(defn- add-inverse
  [m]
  (into m (for [[k v] m] [v k])))

; curved rails are tricky with their direction:
; a full circle looks like this (starting with at 01:30 o'clock):
; 3 0 5 2 7 4 1 6 

(def directions
  ; direction, axis to negate, direction to change; direction to change for curved rails
  {:vertically [:x {0 0, 1 7, 2 6, 3 5, 4 4, 5 3, 6 2, 7 1} (add-inverse {0 1, 2 7, 3 6, 4 5})]
   :horizontally [:y {0 4, 1 3, 2 2, 3 1, 4 0, 5 7, 6 6, 7 5} (add-inverse {0 5, 1 4, 2 3, 6 7})]})

(defn- mirror-direction
  [mapping direction]
  (mapping (or direction 0)))

(def priority-mapping
  (add-inverse {"left" "right"}))

(def priority-keys
  #{:output_priority :input_priority})

(defn mirror-position
  [p axis]
  (update-in p [:position axis] -))

(defn mirror-priority
  [e]
  (s/transform [(s/submap priority-keys) s/MAP-VALS] priority-mapping e))

(defn mirror-entity
  [axis dir-map dir-map-curved-rail entity]
  (-> entity
      (update :direction (partial mirror-direction (if (= (:name entity) "curved-rail") dir-map-curved-rail dir-map)))
      (mirror-position axis)
      (mirror-priority)))

(defn mirror-tile
  [axis tile]
  (-> tile
      (mirror-position axis)))

(defn mirror
  [blueprint direction]
  (let [[axis dir-map dir-map-curved-rail] (get directions direction (:vertically directions))]
    (cond->> blueprint
      ; entities
      (get-in blueprint [:blueprint :entities])
      (s/transform [:blueprint :entities s/ALL] (partial mirror-entity axis dir-map dir-map-curved-rail))
      ; tiles
      (get-in blueprint [:blueprint :tiles])
      (s/transform [:blueprint :tiles s/ALL] (partial mirror-tile axis)))))
