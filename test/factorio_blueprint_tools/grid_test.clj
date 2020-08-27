(ns factorio-blueprint-tools.grid-test
  (:require [clojure.test :refer [deftest testing is are]]
            [factorio-blueprint-tools.blueprint :as t]))

(def fixtures
  {:default
   {:blueprint {:entities [{:entity_number 1 :name "transport-belt" :position {:x 432.5 :y -200.5}}] 
                :item "blueprint" 
                :version 281474976710656}}

   :snap
   {:blueprint {:snap-to-grid {:x 1 :y 1} 
                :entities [{:entity_number 1 :name "transport-belt" :position {:x 432.5 :y -200.5}}] 
                :item "blueprint" 
                :version 281474976710656}}

   :absolute 
   {:blueprint {:snap-to-grid {:x 1 :y 1} 
                :absolute-snapping true 
                :icons [{:signal {:type "item" :name "transport-belt"} :index 1}]
                :entities [{:entity_number 1 :name "transport-belt" :position {:x 0.5 :y 0.5}}] 
                :item "blueprint" 
                :version 281474976710656}}})
