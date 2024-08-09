(ns factorio-blueprint-tools.entity
  (:require
    [factorio-blueprint-tools.coord :as coord]
    [factorio-blueprint-tools.sizes :as sizes]))


(defn force-direction
  [direction]
  (or direction 0))


(defn selection-box-direction
  [name direction]
  (let [direction (force-direction direction)]
    (if (and (= name "curved-rail") (odd? direction))
      (dec direction)
      direction)))


(defn quick-rotate-selection-box
  [selection-box direction]
  (coord/rotate-box selection-box direction))


(defn save-rotate-selection-box
  [selection-box direction]
  (let [[[x1 y1] [x2 y2]] selection-box
        x-mirror (vector (coord/coord x1 (- y1)) (coord/coord x2 (- y2))) ; not coord/box, because this aligns it back
        selection-box' (quick-rotate-selection-box selection-box direction)
        x-mirror' (quick-rotate-selection-box x-mirror direction)]
    (coord/union-box selection-box' x-mirror')))


(defn rotate-selection-box
  [selection-box name direction]
  (let [direction (selection-box-direction name direction)]
    ((if (odd? direction) save-rotate-selection-box quick-rotate-selection-box) selection-box direction)))


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
