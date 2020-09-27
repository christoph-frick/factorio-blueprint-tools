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

(defn has-items?
  [path blueprint]
  (-> (get-in blueprint path) (seq) (boolean)))

(def entities-get-in
  [:blueprint :entities])

(def entities-path
  (conj entities-get-in s/ALL))

(defn has-entities?
  [blueprint]
  (has-items? entities-get-in blueprint))

(defn entities
  [blueprint]
  (s/select entities-path blueprint))

(def tiles-get-in
  [:blueprint :tiles])

(def tiles-path
  (conj tiles-get-in s/ALL))

(defn has-tiles?
  [blueprint]
  (has-items? tiles-get-in blueprint))

(defn tiles
  [blueprint]
  (s/select tiles-path blueprint))

(defn area
  [box-extractor-fn items]
  (transduce
   (map box-extractor-fn)
   (completing coord/union-box)
   coord/NIL-BOX
   items))

(defn positions-area
  [blueprint]
  (area
   #(let [pos (entity-coord %)] (coord/box pos pos))
   (entities blueprint)))

(defn entities-area
  [blueprint]
  (area
   entity-area
   (entities blueprint)))

(defn tiles-area
  [blueprint]
  (area
   #(let [pos (entity-coord %)]
      (coord/box
        pos
        (coord/translate-coord pos coord/ONE)))
   (tiles blueprint)))

(defn blueprint-area
  "Area of entities and tiles"
  [blueprint]
  (case ((juxt has-entities? has-tiles?) blueprint)
    [true false]
    (entities-area blueprint)

    [false true]
    (tiles-area blueprint)

    [true true]
    (coord/union-box
     (entities-area blueprint)
     (tiles-area blueprint))))

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

(defn blueprint?
  [maybe-blueprint]
  (contains? maybe-blueprint :blueprint))

(defn blueprint-book?
  [blueprint-or-book]
  (contains? blueprint-or-book :blueprint_book))

(defn map-blueprint-or-book
  [f blueprint-or-book]
  (if (blueprint-book? blueprint-or-book)
    (s/transform [:blueprint_book :blueprints s/ALL] (partial map-blueprint-or-book f) blueprint-or-book)
    (f blueprint-or-book)))

(defn snap-to-grid?
  [blueprint]
  (map? (some-> blueprint :blueprint :snap-to-grid)))

(defn absolute-snapping?
  [blueprint]
  (= true (some-> blueprint :blueprint :absolute-snapping)))

(defn snap
  "Configured snapping of a blueprint: :absolute, :snap, :default"
  [blueprint]
  (case ((juxt snap-to-grid? absolute-snapping?) blueprint)
    [true true] :absolute
    [true false] :snap
    :default))

(defn snap-grid
  [blueprint]
  (s/select [:blueprint :snap-to-grid (s/multi-path :x :y)] blueprint))

(defn set-snap-grid
  [blueprint x y]
  (update-in blueprint [:blueprint :snap-to-grid] assoc :x x :y y))
