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
                :entities [{:entity_number 1 :name "transport-belt" :position {:x 0.5 :y 0.5}}] 
                :item "blueprint" 
                :version 281474976710656}}})

(deftest test-snap
  (are [expected]
       (= expected (t/snap (expected fixtures)))
       :default
       :snap
       :absolute))

(deftest test-snap-grid
  (is (= [1 1] (t/snap-grid (:snap fixtures)))))

(def test-set-snap-grid
  (is (= [2 3] 
         (t/snap-grid 
           (t/set-snap-grid (:snap fixtures) 2 3)))))
