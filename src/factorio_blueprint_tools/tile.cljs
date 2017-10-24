(ns factorio-blueprint-tools.tile
  (:require [com.rpl.specter :as s]
            [factorio-blueprint-tools.sizes :as sizes]
            [factorio-blueprint-tools.coord :as coord]))

(defn fix-entity-numbers
  [entities]
  (mapv (fn [entity number]
          (assoc entity :entity_number number))
        entities
        (iterate inc 1)))

(defn- entity-area
  [{:keys [name position direction] :as entity}]
  (-> (sizes/selection-box name)
      (coord/rotate-box direction)
      (coord/translate-box (coord/coord (:x position) (:y position)))))

(defn entities-area
  [blueprint]
  (transduce
   (map entity-area)
   (completing coord/union-box)
   (s/select [:blueprint :entities s/ALL (s/submap [:name :position :direction])] blueprint)))

(defn move-position
  [position x-offset y-offset]
  (s/multi-transform [:position (s/multi-path [:x (s/terminal #(+ % x-offset))]
                                              [:y (s/terminal #(+ % y-offset))])]
                     position))

(defn tile
  [blueprint x-times y-times]
  (if-let [area (entities-area blueprint)]
    (let [[[min-x min-y] [max-x max-y]] area
          width (Math/ceil (- max-x min-x))
          height (Math/ceil (- max-y min-y))
          tiled-entities (into [] cat (for [y (range y-times)
                                            x (range x-times)]
                                        (map #(move-position % (* x width) (* y height)) (-> blueprint :blueprint :entities))))]
      (assoc-in blueprint [:blueprint :entities] (fix-entity-numbers tiled-entities)))
    blueprint))
