(ns factorio-blueprint-tools.landfill
  (:require [com.rpl.specter :as s]
            [factorio-blueprint-tools.blueprint :as blueprint]))

(defn landfill-1
  [blueprint]
  (if-let [area (blueprint/entities-area blueprint)]
    (let [[[min-x min-y] [max-x max-y]] area
          landfill (for [y (range (Math/floor min-y) (inc (Math/ceil max-y)) 1)
                         x (range (Math/floor min-x) (inc (Math/ceil max-x)) 1)]
                     {:position {:x x :y y} :name "landfill"})]
      (assoc-in blueprint [:blueprint :tiles] landfill))
    blueprint))

(defn landfill
  [blueprint-or-book]
  (blueprint/map-blueprint-or-book landfill-1 blueprint-or-book))
