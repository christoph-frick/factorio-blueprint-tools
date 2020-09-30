(ns factorio-blueprint-tools.mirror-test
  (:require [clojure.test :refer [deftest testing is are]]
            [factorio-blueprint-tools.mirror :as t]))

(deftest test-mirror-tiles-remain-under-items
  (let [orig {:blueprint {:tiles [{:name "stone-path", :position {:x 58, :y -9}}
                                  {:name "concrete", :position {:x 61, :y -9}}]}}
        target {:blueprint {:tiles [{:name "stone-path", :position {:x -59, :y -9}}
                                    {:name "concrete", :position {:x -62, :y -9}}]}}]
    (is (= (t/mirror orig :vertically) target))
    (is (= (t/mirror (t/mirror orig :vertically) :vertically) orig))))

(deftest test-move-back-into-absolute-grid
  (let [orig {:blueprint {:snap-to-grid {:x 4, :y 5},
                          :absolute-snapping true,
                          :entities [{:entity_number 1, :direction 0, :name "transport-belt", :position {:x 0.5, :y 1.5}}
                                     {:entity_number 2, :direction 0, :name "transport-belt", :position {:x 0.5, :y 0.5}}
                                     {:entity_number 3, :direction 0, :name "transport-belt", :position {:x 0.5, :y 3.5}}
                                     {:entity_number 4, :direction 0, :name "transport-belt", :position {:x 0.5, :y 2.5}}
                                     {:entity_number 5, :direction 0, :name "transport-belt", :position {:x 1.5, :y 4.5}}
                                     {:entity_number 6, :direction 0, :name "transport-belt", :position {:x 0.5, :y 4.5}}
                                     {:entity_number 7, :direction 0, :name "transport-belt", :position {:x 3.5, :y 4.5}}
                                     {:entity_number 8, :direction 0, :name "transport-belt", :position {:x 2.5, :y 4.5}}],
                          :tiles [{:position {:x 2, :y 0}, :name "concrete"}
                                  {:position {:x 3, :y 0}, :name "concrete"}
                                  {:position {:x 3, :y 1}, :name "concrete"}]}}]
    (is (= orig (t/mirror (t/mirror orig :vertically) :vertically)))))

(deftest test-mirroring-train-stations
  (are [direction orig target] (= target (t/mirror orig direction))
    :vertically
    {:blueprint {:entities [{:entity_number 2, :name "straight-rail", :position {:x -65, :y -143}, :direction 0}
                            {:entity_number 3, :name "train-stop",    :position {:x -63, :y -143}, :direction 0, :station ""}]}}
    {:blueprint {:entities [{:entity_number 2, :name "straight-rail", :position {:x 65, :y -143}, :direction 0}
                            {:entity_number 3, :name "train-stop",    :position {:x 63, :y -143}, :direction 4, :station ""}]}}

    :horizontally
    {:blueprint {:entities [{:entity_number 1, :name "train-stop",    :position {:x -71, :y -137}, :direction 6, :station ""}
                            {:entity_number 2, :name "straight-rail", :position {:x -71, :y -135}, :direction 2}]}}
    {:blueprint {:entities [{:entity_number 1, :name "train-stop",    :position {:x -71, :y 137}, :direction 2, :station ""}
                            {:entity_number 2, :name "straight-rail", :position {:x -71, :y 135}, :direction 2}]}}))
