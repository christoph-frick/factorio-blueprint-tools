(ns factorio-blueprint-tools.landfill-test
  (:require [clojure.test :refer [deftest is are]]
            [factorio-blueprint-tools.blueprint :as blueprint]
            [factorio-blueprint-tools.landfill :as sut]))

(deftest test-full-sparse-happy-path
  (let [bp {:blueprint {:entities [{:entity_number 1, :name "transport-belt", :position {:x 78.5, :y -58.5}}
                                   {:entity_number 2, :name "transport-belt", :position {:x 80.5, :y -58.5}}
                                   {:entity_number 3, :name "underground-belt", :position {:x 78.5, :y -57.5}, :type "output"}
                                   {:entity_number 4, :name "underground-belt", :position {:x 80.5, :y -57.5}, :type "output"}
                                   {:entity_number 5, :name "fast-inserter", :position {:x 78.5, :y -54.5}, :direction 4}
                                   {:entity_number 6, :name "fast-inserter", :position {:x 78.5, :y -55.5}}
                                   {:entity_number 7, :name "fast-inserter", :position {:x 80.5, :y -54.5}, :direction 4}
                                   {:entity_number 8, :name "fast-inserter", :position {:x 80.5, :y -55.5}}
                                   {:entity_number 9, :name "underground-belt", :position {:x 78.5, :y -52.5}, :type "input"}
                                   {:entity_number 10, :name "underground-belt", :position {:x 80.5, :y -52.5}, :type "input"}],
                        :item "blueprint",
                        :version 281474976710656}}
        full (sut/landfill {:mode :full} bp)
        sparse (sut/landfill {:mode :sparse} bp)]
    (is (= 21 (count (-> full :blueprint :tiles))))
    (is (= (count (-> bp :blueprint :entities)) (count (-> sparse :blueprint :tiles))))))

(deftest test-tile-modes
  (let [bp {:blueprint {:entities [{:entity_number 1,
                                    :name "gun-turret",
                                    :position {:x 144, :y 69}}],
                        :tiles [{:position {:x 142, :y 67},
                                 :name "hazard-concrete-left"}
                                {:position {:x 142, :y 68},
                                 :name "hazard-concrete-left"}
                                {:position {:x 142, :y 69},
                                 :name "hazard-concrete-left"}
                                {:position {:x 142, :y 70},
                                 :name "hazard-concrete-left"}
                                {:position {:x 143, :y 67},
                                 :name "hazard-concrete-left"}
                                {:position {:x 143, :y 68},
                                 :name "hazard-concrete-left"}
                                {:position {:x 143, :y 69},
                                 :name "hazard-concrete-left"}
                                {:position {:x 143, :y 70},
                                 :name "hazard-concrete-left"}
                                {:position {:x 144, :y 67},
                                 :name "hazard-concrete-left"}
                                {:position {:x 144, :y 68},
                                 :name "hazard-concrete-left"}
                                {:position {:x 144, :y 69},
                                 :name "hazard-concrete-left"}
                                {:position {:x 144, :y 70},
                                 :name "hazard-concrete-left"}
                                {:position {:x 145, :y 67},
                                 :name "hazard-concrete-left"}
                                {:position {:x 145, :y 68},
                                 :name "hazard-concrete-left"}
                                {:position {:x 145, :y 69},
                                 :name "hazard-concrete-left"}
                                {:position {:x 145, :y 70},
                                 :name "hazard-concrete-left"}],
                        :item "blueprint"}}]
    (are [tile-mode path area] (= area
                                  (blueprint/area
                                   blueprint/entity-pos-to-box
                                   (get-in
                                    (sut/landfill {:tile-mode tile-mode} bp)
                                    path)))
      :remove
      [:blueprint :tiles]
      [[143 68] [144 69]]

      :replace
      [:blueprint :tiles]
      [[142 67] [145 70]]

      :to-book
      [:blueprint_book :blueprints 0 :blueprint :tiles]
      [[142 67] [145 70]])))
