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

(defn entities
  [blueprint]
  (s/select [:blueprint :entities s/ALL] blueprint))

(defn area
  [box-extractor-fn blueprint]
  (transduce
   (map box-extractor-fn)
   (completing coord/union-box)
   (entities blueprint)))

(defn positions-area
  [blueprint]
  (area
   #(let [pos (entity-coord %)] (coord/box pos pos))
   blueprint))

(defn entities-area
  [blueprint]
  (area
   entity-area
   blueprint))

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

(defn is-book?
  [blueprint-or-book]
  (contains? blueprint-or-book :blueprint_book))

(defn map-blueprint-or-book
  [f blueprint-or-book]
  (if (is-book? blueprint-or-book)
    (s/transform [:blueprint_book :blueprints s/ALL] f blueprint-or-book)
    (f blueprint-or-book)))
