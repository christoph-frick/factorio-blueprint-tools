(ns factorio-blueprint-tools.blueprint
  (:require [com.rpl.specter :as s]
            [factorio-blueprint-tools.sizes :as sizes]
            [factorio-blueprint-tools.coord :as coord]))

(defn fix-entity-numbers
  [entities]
  (mapv (fn [entity number]
          (assoc entity :entity_number number))
        entities
        (iterate inc 1)))

(defn entity-coord
  [{:keys [position] :as entity}]
  (coord/coord (:x position) (:y position)))

(defn entity-area
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

(defn book
  [label blueprints]
  {:blueprint_book
   {:item "blueprint-book"
    :label label
    :blueprints (map-indexed
                 (fn [idx blueprint]
                   (assoc blueprint :index idx))
                 blueprints)}})
