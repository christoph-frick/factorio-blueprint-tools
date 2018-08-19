(ns factorio-blueprint-tools.mirror
  (:require [com.rpl.specter :as s]
            [factorio-blueprint-tools.blueprint :as blueprint]))

(def mirror-directions
  {0 0
   1 7
   2 6
   3 5
   4 4
   5 3
   6 2
   7 1})

(defn- mirror-direction
  [direction]
  (mirror-directions (or direction 0)))

(defn mirror
  [blueprint]
  (s/transform [:blueprint :entities s/ALL]
               (fn [entity]
                 (-> entity
                     (update :direction mirror-direction)
                     (update-in [:position :x] -)))
               blueprint))
