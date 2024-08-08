(ns factorio-blueprint-tools.split
    (:require
        [factorio-blueprint-tools.coord :as coord]
        [factorio-blueprint-tools.entity :as entity]
        [factorio-blueprint-tools.blueprint :as blueprint]))

(defn split
  [blueprint tile-size]
  (when-let [area (blueprint/positions-area blueprint)]
    (let  [label (or (some-> blueprint :blueprint :label) "Anonymous blueprint")
           area (-> area
                    (coord/translate-box (coord/coord -1))
                    (coord/expand-box coord/ONE))
           [abs-offset _] area
           tile-size-half (Math/floor (/ tile-size 2))
           [width height] (coord/area area)
           x-tiles (Math/ceil (/ width tile-size))
           y-tiles (Math/ceil (/ height tile-size))
           align-fn (partial coord/transform-coord #(let [i (Math/floor %)] (if (odd? i) (dec i) i)))
           blueprints (for [y (range y-tiles)
                            x (range x-tiles)]
                        (let [filter-box (-> (coord/box-from-size coord/ZERO tile-size tile-size)
                                             (coord/translate-box (coord/coord (* x tile-size) (* y tile-size)))
                                             (coord/translate-box abs-offset)
                                             (coord/translate-box (coord/coord x y))
                                             (coord/expand-box coord/ONE))
                              [offset _] filter-box
                              [correction-x correction-y] (-> (coord/coord (- tile-size-half))
                                                              (coord/translate-coord (coord/negate-coord offset))
                                                              (align-fn))]
                          (-> blueprint
                              (assoc-in [:blueprint :label] (str label " [" x "|" y "]"))
                              (update-in [:blueprint :entities]
                                         (fn [entities]
                                           (->> entities
                                                (filter (partial entity/in-box? filter-box))
                                                (map #(blueprint/move-position % correction-x correction-y))
                                                (blueprint/fix-entity-numbers)))))))]
      (blueprint/book (str label " " x-tiles " x " y-tiles) blueprints))))
