(ns factorio-blueprint-tools.landfill
  (:require [com.rpl.specter :as s]
            [factorio-blueprint-tools.blueprint :as blueprint]
            [factorio-blueprint-tools.coord :as coord]))

(defn set-landfill-tiles
  [blueprint poss]
  (assoc-in blueprint
            [:blueprint :tiles]
            (for [[x y] poss]
              {:position {:x x :y y} :name "landfill"})))

(defn landfill-area-to-tile-pos
  [area]
  (let [[[min-x min-y] [max-x max-y]] area]
    (for [y (range (Math/floor min-y) (Math/ceil max-y) 1)
          x (range (Math/floor min-x) (Math/ceil max-x) 1)]
      [x y])))

; TODO this needs special cases for at least:
; - curved rails
; No need for:
; - offshore pumps: the position seems fine; the box is to big though

(defmulti landfill-sparse :name)

(defmethod landfill-sparse :default
  [entity]
  (landfill-area-to-tile-pos (blueprint/entity-area entity)))

(defn landfill-sparse-1
  [blueprint]
  (if-let [entites (blueprint/entities blueprint)]
    (let [landfill (into #{} (mapcat landfill-sparse) entites)]
      (set-landfill-tiles blueprint landfill))
    blueprint))

(defn landfill-full-1
  [blueprint]
  (if-let [area (blueprint/entities-area blueprint)]
    (set-landfill-tiles blueprint (landfill-area-to-tile-pos area))
    blueprint))

(defn landfill
  [{:keys [mode]} blueprint-or-book]
  (blueprint/map-blueprint-or-book
   (if (= mode :sparse)
     landfill-sparse-1
     landfill-full-1)
   blueprint-or-book))
