(ns factorio-blueprint-tools.mirror
  (:require [com.rpl.specter :as s]
            [factorio-blueprint-tools.entity :as entity]
            [factorio-blueprint-tools.blueprint :as blueprint]))

(defn- add-inverse
  [m]
  (into m (for [[k v] m] [v k])))

(defn get-or-default
  "Similar to get, but get the default-key from the map as default"
  [map key default-key]
  (get map key (get map default-key)))

(def direction-to-axis
  {:vertically :x
   :horizontally :y})

(def axis-to-coord
  {:x 0
   :y 1})

; curved rails are tricky with their direction:
; a full circle looks like this (starting with at 01:30 o'clock):
; 3 0 5 2 7 4 1 6

; tanks need to rotate for 90° instead (the 180° just looks the same)

(def direction-to-mirror-config
  {:vertically {"curved-rail" (add-inverse {0 1, 2 7, 3 6, 4 5})
                "storage-tank" {0 2, 2 4, 4 6, 6 0}
                "train-stop" (add-inverse {0 4, 2 6})
                :default {0 0, 1 7, 2 6, 3 5, 4 4, 5 3, 6 2, 7 1}}
   :horizontally {"curved-rail" (add-inverse {0 5, 1 4, 2 3, 6 7})
                  "storage-tank" {0 2, 2 4, 4 6, 6 0}
                  "train-stop" (add-inverse {0 4, 2 6})
                  :default {0 4, 1 3, 2 2, 3 1, 4 0, 5 7, 6 6, 7 5}}})

(defn- mirror-direction
  [mapping direction]
  (mapping (entity/force-direction direction)))

(def priority-mapping
  (add-inverse {"left" "right"}))

(def priority-keys
  #{:output_priority :input_priority})

(defn mirror-position
  [p axis correction]
  (update-in p [:position axis] #(+ correction (- %))))

(defn mirror-priority
  [e]
  (s/transform [(s/submap priority-keys) s/MAP-VALS] priority-mapping e))

(defn mirror-entity
  [axis mirror-config correction entity]
  (-> entity
      (update :direction (partial mirror-direction (get-or-default mirror-config (:name entity) :default)))
      (mirror-position axis correction)
      (mirror-priority)))

(defn mirror-tile
  [axis correction tile]
  (mirror-position tile axis correction))

(defn mirror
  [blueprint direction]
  (let [axis (get-or-default direction-to-axis direction :vertically)
        mirror-config (get-or-default direction-to-mirror-config direction :vertically)
        correction (if (blueprint/absolute-snapping? blueprint)
                     (get (blueprint/snap-grid blueprint) (axis-to-coord axis))
                     0)]
    (cond->> blueprint
      ; entities
      (blueprint/has-entities? blueprint)
      (s/transform blueprint/entities-path (partial mirror-entity axis mirror-config correction))
      ; tiles
      (blueprint/has-tiles? blueprint)
      (s/transform blueprint/tiles-path (partial mirror-tile axis (dec correction))))))
