(ns factorio-blueprint-tools.landfill
  (:require [factorio-blueprint-tools.blueprint :as blueprint]
            [factorio-blueprint-tools.entity :as entity]
            [factorio-blueprint-tools.coord :as coord]))

(defn landfill-tile
  [[x y]]
  {:position {:x x :y y}
   :name "landfill"})

(defn sort-pos
  [[ax ay] [bx by]]
  (let [y (compare ay by)]
    (if (zero? y)
      (compare ax bx)
      y)))

(defn sorted-pos
  [poss]
  (apply sorted-set-by sort-pos poss))

(defn set-landfill-tiles
  [blueprint poss]
  (blueprint/set-tiles blueprint
                       (mapv landfill-tile
                             (sorted-pos poss))))

(defn min-max-range
  [min max]
  (range (Math/floor (Math/min min max))
         (Math/ceil (Math/max min max))
         1))

(defn landfill-area-to-tile-pos
  [area]
  (let [[[min-x min-y] [max-x max-y]] area]
    (set
     (for [y (min-max-range min-y max-y)
           x (min-max-range min-x max-x)]
       [(int x) (int y)]))))

(defmulti landfill-entity :name)

(def offshore-pump-offsets
  {0 [ 0  1]
   2 [-1  0]
   4 [ 0 -1]
   6 [ 1  0]})

(defmethod landfill-entity "offshore-pump"
  [entity]
  (let [pos (entity/coord entity)
        ofs (offshore-pump-offsets (:direction entity 0))]
    (landfill-area-to-tile-pos [pos (coord/translate-coord pos ofs)])))

(defmethod landfill-entity :default
  [entity]
  (-> entity entity/area landfill-area-to-tile-pos))

(defn landfill-sparse-entities
  [blueprint]
  (if-let [entites (blueprint/entities blueprint)]
    (into #{} (mapcat landfill-entity) entites)
    #{}))

(defn landfill-sparse-tiles
  [blueprint]
  (if (blueprint/has-tiles? blueprint)
    (set (map entity/coord (blueprint/tiles blueprint)))
    #{}))

(defn landfill-full-entities
  [blueprint]
  (if-let [area (blueprint/entities-area blueprint)]
    (landfill-area-to-tile-pos area)
    #{}))

(defn landfill-full-tiles
  [blueprint]
  (if (blueprint/has-tiles? blueprint)
    (landfill-area-to-tile-pos (blueprint/tiles-area blueprint))
    #{}))

(declare to-book-1)

(defn landfill-1
  [fill-mode tile-mode blueprint]
  (if (= tile-mode :to-book)
    (to-book-1 fill-mode blueprint)
    (let [entitiies-dispatch {:full landfill-full-entities
                              :sparse landfill-sparse-entities}
          tiles-dispatch {:full landfill-full-tiles
                          :sparse landfill-sparse-tiles}
          tiles ((entitiies-dispatch fill-mode) blueprint)
          tiles (if (= tile-mode :replace)
                  (into tiles ((tiles-dispatch fill-mode) blueprint))
                  tiles)]
      (set-landfill-tiles blueprint tiles))))

(defn to-book-1
  [fill-mode blueprint]
  (blueprint/book "Landfill"
                  [(update
                    (landfill-1 fill-mode :replace blueprint)
                    :blueprint
                    dissoc :entities)
                   blueprint]))

(defn landfill
  [{:keys [fill-mode tile-mode] :or {fill-mode :sparse tile-mode :replace}} blueprint-or-book]
  (blueprint/map-blueprint-or-book
   (partial landfill-1 fill-mode tile-mode)
   blueprint-or-book))
