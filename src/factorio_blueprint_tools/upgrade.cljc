(ns factorio-blueprint-tools.upgrade
  (:require [com.rpl.specter :as s]
            [clojure.string :as str]))

(def upgrades [; higher tiers are more right
               ["transport-belt" "fast-transport-belt" "express-transport-belt"]
               ["underground-belt" "fast-underground-belt" "express-underground-belt"]
               ["splitter" "fast-splitter" "express-splitter"]
               ["assembling-machine-1" "assembling-machine-2" "assembling-machine-3"]
               ["burner-inserter" "inserter" "fast-inserter" "stack-inserter"]
               ["filter-inserter" "stack-filter-inserter"]])

(def upgrades-by-key
  (into
   {}
   (for [chain upgrades
         upgrade chain]
     [upgrade chain])))

(def upgrades-order
  (into [] cat upgrades))

(defn- nice-entity-name
  [entity-name]
  (-> entity-name
      (str/replace "-" " ")
      (str/replace #"^[a-z]" #(.toUpperCase %))))

(def upgrades-names
  (into
   {}
   (map (juxt identity nice-entity-name))
   upgrades-order))

(def default-upgrade-config
  (into
   {}
   (for [[upgrade chain] upgrades-by-key]
     [upgrade (peek chain)])))

(def upgradables
  (set
   (keys upgrades-by-key)))

(def upgrade-entity-path
  [:blueprint :entities s/ALL :name])

(defn upgradeable-from-blueprint
  "Returns a set of entity names, that may be upgraded from a blueprint"
  [blueprint]
  (into
   #{}
   (filter upgradables)
   (s/select upgrade-entity-path blueprint)))

(defn upgrade-blueprint
  "Replace the entity names in a blueprint given a map of from-key and to-value"
  [upgrade-config blueprint]
  (s/transform upgrade-entity-path #(get upgrade-config % %) blueprint))
