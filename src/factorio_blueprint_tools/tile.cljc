(ns factorio-blueprint-tools.tile
  (:require [com.rpl.specter :as s]
            [factorio-blueprint-tools.blueprint :as blueprint]))

(defn tile
  [blueprint x-times y-times]
  (if-let [area (blueprint/entities-area blueprint)]
    (let [[[min-x min-y] [max-x max-y]] area
          width (Math/ceil (- max-x min-x))
          height (Math/ceil (- max-y min-y))
          tiled-entities (into [] cat (for [y (range y-times)
                                            x (range x-times)]
                                        (map #(blueprint/move-position % (* x width) (* y height)) (-> blueprint :blueprint :entities))))]
      (assoc-in blueprint [:blueprint :entities] (blueprint/fix-entity-numbers tiled-entities)))
    blueprint))
