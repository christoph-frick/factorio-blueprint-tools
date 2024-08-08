(ns factorio-blueprint-tools.tile
  (:require [factorio-blueprint-tools.blueprint :as blueprint]))

(defn width-and-height
  [blueprint]
  (case (blueprint/snap blueprint)
    :default
    (let [area (blueprint/blueprint-area blueprint)
          [[min-x min-y] [max-x max-y]] area
          width (Math/ceil (- max-x min-x))
          height (Math/ceil (- max-y min-y))]
      [width height])

    (blueprint/snap-grid blueprint)))

(defn tile-items
  [items x-times y-times width height]
  (into [] cat (for [y (range y-times)
                     x (range x-times)]
                 (map #(blueprint/move-position % (* x width) (* y height)) items))))

(defn tile-entities
  [blueprint x-times y-times width height]
  (update-in
   blueprint
   blueprint/entities-get-in
   #(blueprint/fix-entity-numbers (tile-items % x-times y-times width height))))

(defn tile-tiles
  [blueprint x-times y-times width height]
  (update-in
   blueprint
   blueprint/tiles-get-in
   #(tile-items % x-times y-times width height)))

(defn tile
  [blueprint x-times y-times]
  (if (blueprint/blueprint? blueprint)
    (let [[width height] (width-and-height blueprint)]
      (cond-> blueprint
        (blueprint/has-entities? blueprint)
        (tile-entities x-times y-times width height)

        (blueprint/has-tiles? blueprint)
        (tile-tiles x-times y-times width height)

        (not= :default (blueprint/snap blueprint))
        (blueprint/set-snap-grid (* width x-times) (* width y-times))))
    blueprint))
