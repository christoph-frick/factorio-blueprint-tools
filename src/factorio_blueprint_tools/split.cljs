(ns factorio-blueprint-tools.split
  (:require [com.rpl.specter :as s]
            [factorio-blueprint-tools.coord :as coord]
            [factorio-blueprint-tools.blueprint :as blueprint]))

(defn entity-in-box?
  [box entity]
  (coord/in-box? box (blueprint/entity-coord entity)))

(defn split
  [blueprint tile-size]
  (when-let [area (blueprint/entities-area blueprint)]
    (let  [label (or (some-> blueprint :blueprint :label) "Anonymous blueprint")
           [[min-x min-y] [max-x max-y]] area
           tile-size-half (Math/floor (/ tile-size 2))
           width (Math/ceil (- max-x min-x))
           height (Math/ceil (- max-y min-y))
           x-tiles (Math/ceil (/ width tile-size))
           y-tiles (Math/ceil (/ height tile-size))
           blueprints (for [y (range y-tiles)
                            x (range x-tiles)]
                        (let [filter-box (-> (coord/box-from-size coord/ZERO tile-size tile-size)
                                             (coord/translate-box (coord/coord (* x tile-size) (* y tile-size)))
                                             (coord/translate-box (coord/coord min-x min-y)))
                              [[offset-x offset-y] _] filter-box]
                          (-> blueprint
                              (assoc-in [:blueprint :label] (str label " [" x "|" y "]"))
                              (update-in [:blueprint :entities]
                                         (fn [entities]
                                           (->> entities
                                                (filter (partial entity-in-box? filter-box))
                                                (map #(blueprint/move-position % (- (- tile-size-half) offset-x) (- (- tile-size-half) offset-y)))
                                                (blueprint/fix-entity-numbers)))))))]
      (blueprint/book (str label " " x-tiles " x " y-tiles) blueprints))))
