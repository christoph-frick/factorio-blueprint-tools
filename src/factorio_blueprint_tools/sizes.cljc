(ns factorio-blueprint-tools.sizes
  #?(:cljs (:require-macros [factorio-blueprint-tools.macros :as m])
     :clj (:require [factorio-blueprint-tools.macros :as m])))

(defn lookup
  [storage entity-name fallback]
  (let [entity-kw (keyword entity-name)]
    (if (contains? storage entity-kw)
      (storage entity-kw)
      (do
        (print "Unknown entity" entity-name)
        fallback))))

(def selection-boxes
  (assoc
   (m/load-edn "selection-boxes.edn")
   :curved-rail [[-2.5 -4.0] [2 4]]))

(def selection-box-fallback [[-0.5 -0.5] [0.5 0.5]])

(defn selection-box
  [entity-name]
  (lookup selection-boxes entity-name selection-box-fallback))

(def stack-sizes
  (m/load-edn "stack-sizes.edn"))

(defn stack-size
    [entity-name]
    (lookup stack-sizes entity-name 500))
