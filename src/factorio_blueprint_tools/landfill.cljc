(ns factorio-blueprint-tools.landfill
  (:require [factorio-blueprint-tools.blueprint :as blueprint]))

(defn set-landfill-tiles
  [blueprint poss]
  (assoc-in blueprint
            [:blueprint :tiles]
            (for [[x y] (apply sorted-set-by
                               (fn [[ax ay] [bx by]]
                                 (let [y (compare ay by)]
                                   (if (zero? y)
                                     (compare ax bx)
                                     y)))
                               poss)]
              {:position {:x x :y y} :name "landfill"})))

(defn landfill-area-to-tile-pos
  [area]
  (let [[[min-x min-y] [max-x max-y]] area]
    (into #{}
          (for [y (range (Math/floor min-y) (Math/ceil max-y) 1)
                x (range (Math/floor min-x) (Math/ceil max-x) 1)]
            [(int x) (int y)]))))

(defn landfill-sparse-entities
  [blueprint]
  (if-let [entites (blueprint/entities blueprint)]
    (into #{} (mapcat (comp landfill-area-to-tile-pos blueprint/entity-area)) entites)
    #{}))

(defn landfill-sparse-tiles
  [blueprint]
  (if (blueprint/has-tiles? blueprint)
    (into #{} (map blueprint/entity-coord (blueprint/tiles blueprint)))
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
  [mode tile-mode blueprint]
  (if (= tile-mode :to-book)
    (to-book-1 mode blueprint)
    (let [entitiies-dispatch {:full landfill-full-entities
                              :sparse landfill-sparse-entities}
          tiles-dispatch {:full landfill-full-tiles
                          :sparse landfill-sparse-tiles}
          tiles ((entitiies-dispatch mode) blueprint)
          tiles (if (= tile-mode :replace)
                  (into tiles ((tiles-dispatch mode) blueprint))
                  tiles)]
      (set-landfill-tiles blueprint tiles))))

(defn to-book-1
  [mode blueprint]
  (blueprint/book "Landfill"
                  [(update
                    (landfill-1 mode :replace blueprint)
                    :blueprint
                    dissoc :entities)
                   blueprint]))

(defn landfill
  [{:keys [mode tile-mode] :or {mode :sparse tile-mode :replace}} blueprint-or-book]
  (blueprint/map-blueprint-or-book
   (partial landfill-1 mode tile-mode)
   blueprint-or-book))
