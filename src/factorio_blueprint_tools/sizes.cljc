(ns factorio-blueprint-tools.sizes
  #?(:cljs (:require-macros [factorio-blueprint-tools.macros :as m])
     :clj (:require [factorio-blueprint-tools.macros :as m])))

(def selection-boxes
  (assoc
   (m/load-edn "selection-boxes.edn")
   :curved-rail [[-2.5 -4.0] [2 4]]))

(def selection-box-fallback [[-0.5 -0.5] [0.5 0.5]])

(defn selection-box
  [entity-name]
  (let [entity-kw (keyword entity-name)]
    (if (contains? selection-boxes entity-kw)
      (selection-boxes entity-kw)
      (do
        (print "Unknown entity" entity-name)
        selection-box-fallback))))
