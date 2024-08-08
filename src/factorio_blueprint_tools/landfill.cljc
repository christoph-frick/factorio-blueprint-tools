(ns factorio-blueprint-tools.landfill
  (:require [factorio-blueprint-tools.blueprint :as blueprint]
            [factorio-blueprint-tools.entity :as entity]
            [factorio-blueprint-tools.coord :as coord]))

(def entity-deny-options
    #{"stone-wall" "gate"})

(def default-entity-deny
    entity-deny-options)

(def default-config
  {:fill-mode :full
   :tile-mode :remove
   :entity-deny default-entity-deny})

(defn remove-entities-by-name
  [name-set entities]
  (remove (comp name-set :name) entities))

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

(defn entities
  [entity-deny blueprint]
  (remove-entities-by-name entity-deny (blueprint/entities blueprint)))

(defn landfill-sparse-entities
  [entity-deny blueprint]
  (if-let [entites (entities entity-deny blueprint)]
    (into #{} (mapcat landfill-entity) entites)
    #{}))

(defn landfill-sparse-tiles
  [blueprint]
  (if (blueprint/has-tiles? blueprint)
    (set (map entity/coord (blueprint/tiles blueprint)))
    #{}))

(defn landfill-full-entities
  [entity-deny blueprint]
  (let [area (blueprint/area entity/area (entities entity-deny blueprint))]
    (if (not= coord/NIL-BOX area)
      (landfill-area-to-tile-pos area)
      #{})))

(defn landfill-full-tiles
  [blueprint]
  (if (blueprint/has-tiles? blueprint)
    (landfill-area-to-tile-pos (blueprint/tiles-area blueprint))
    #{}))

(declare to-book-1)

(defn landfill-1
  [fill-mode tile-mode entity-deny blueprint]
  (if (= tile-mode :to-book)
    (to-book-1 fill-mode entity-deny blueprint)
    (let [entitiies-dispatch {:full landfill-full-entities
                              :sparse landfill-sparse-entities}
          tiles-dispatch {:full landfill-full-tiles
                          :sparse landfill-sparse-tiles}
          tiles ((entitiies-dispatch fill-mode) entity-deny blueprint)
          tiles (if (= tile-mode :replace)
                  (into tiles ((tiles-dispatch fill-mode) blueprint))
                  tiles)]
      (set-landfill-tiles blueprint tiles))))

(defn to-book-1
  [fill-mode entity-deny blueprint]
  (blueprint/book "Landfill"
                  [(update
                    (landfill-1 fill-mode :replace entity-deny blueprint)
                    :blueprint
                    dissoc :entities)
                   blueprint]))

(defn landfill
  [cfg blueprint-or-book]
  (let [{:keys [fill-mode tile-mode entity-deny]} (merge default-config cfg)]
    (blueprint/map-blueprint-or-book
      (partial landfill-1 fill-mode tile-mode entity-deny)
      blueprint-or-book)))
