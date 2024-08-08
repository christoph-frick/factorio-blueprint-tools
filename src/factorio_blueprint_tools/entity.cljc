(ns factorio-blueprint-tools.entity
  (:require
    [factorio-blueprint-tools.coord :as coord]
    [factorio-blueprint-tools.sizes :as sizes]))


(defn force-direction
  [direction]
  (or direction 0))


(defn rotate-selection-box
  [selection-box name direction]
  (let [direction (force-direction direction)]
    (coord/rotate-box selection-box
                      (if (and (= name "curved-rail") (odd? direction))
                        (dec direction)
                        direction))))


(defn coord
  [{:keys [position] :as _entity}]
  (coord/coord (:x position) (:y position)))


(defn area
  [{:keys [name direction] :as entity}]
  (-> (sizes/selection-box name)
      (rotate-selection-box name direction)
      (coord/translate-box (coord entity))))


(defn in-box?
  [box entity]
  (coord/in-box? box (coord entity)))


(defn pos-to-box
  [entity]
  (let [pos (coord entity)]
    (coord/box pos pos)))
