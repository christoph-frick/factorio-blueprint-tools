(ns factorio-blueprint-tools.tile
  (:require [com.rpl.specter :as s]
            [factorio-blueprint-tools.blueprint :as blueprint]))

(defn width-and-height
  [blueprint]
  (case (blueprint/snap blueprint)
    :default
    (let [area (blueprint/entities-area blueprint)
          [[min-x min-y] [max-x max-y]] area
          width (Math/ceil (- max-x min-x))
          height (Math/ceil (- max-y min-y))]
      [width height])
    
    (blueprint/snap-grid blueprint)))

(defn tile
  [blueprint x-times y-times]
  (if (blueprint/blueprint? blueprint)
    (let [[width height] (width-and-height blueprint)
          tiled-entities (into [] cat (for [y (range y-times)
                                            x (range x-times)]
                                        (map #(blueprint/move-position % (* x width) (* y height)) (-> blueprint :blueprint :entities))))
          blueprint' (assoc-in blueprint [:blueprint :entities] (blueprint/fix-entity-numbers tiled-entities))]
      (if-not (= :default (blueprint/snap blueprint'))
        (blueprint/set-snap-grid blueprint' (* width x-times) (* width y-times))
        blueprint'))
    blueprint))
