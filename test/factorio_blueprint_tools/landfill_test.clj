(ns factorio-blueprint-tools.landfill-test
  (:require [clojure.test :refer [deftest is are]]
            [factorio-blueprint-tools.blueprint :as blueprint]
            [factorio-blueprint-tools.entity :as entity]
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
        full (sut/landfill {:fill-mode :full} bp)
        sparse (sut/landfill {:fill-mode :sparse} bp)]
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
                                   entity/pos-to-box
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

(deftest test-landfill-offshore-pump
  (let [bp {:blueprint {:entities [{:entity_number 1,
                                    :name "offshore-pump",
                                    :position {:x -23.5, :y 14.5},
                                    :direction 6}],
                        :item "blueprint"}}]
    (are [dir tiles] (= tiles
                        (->> (assoc-in bp [:blueprint :entities 0 :direction] dir)
                             (sut/landfill {:fill-mode :sparse})
                             :blueprint
                             :tiles
                             (map :position)
                             set))
      0 #{{:x -24, :y 14}
          {:x -24, :y 15}}
      2 #{{:x -24, :y 14}
          {:x -25, :y 14}}
      4 #{{:x -24, :y 14}
          {:x -24, :y 13}}
      6 #{{:x -24, :y 14}
          {:x -23, :y 14}})))

(deftest test-entity-deny
  (let [bp {:blueprint {:entities
                        [{:entity_number 1,
                          :name "stone-wall",
                          :position {:x 239.5, :y -149.5}}
                         {:entity_number 2,
                          :name "stone-wall",
                          :position {:x 240.5, :y -149.5}}
                         {:entity_number 3,
                          :name "gate",
                          :position {:x 241.5, :y -149.5},
                          :direction 2}
                         {:entity_number 4,
                          :name "stone-wall",
                          :position {:x 243.5, :y -149.5}}
                         {:entity_number 5,
                          :name "gate",
                          :position {:x 242.5, :y -149.5},
                          :direction 2}
                         {:entity_number 6,
                          :name "stone-wall",
                          :position {:x 244.5, :y -149.5}}],
                        :item "blueprint",
                        :version 281479278821376}}]
    (are [fill-mode entity-deny landfill-poss]

         (= landfill-poss
            (->> (sut/landfill {:fill-mode fill-mode
                                :entity-deny entity-deny}
                               bp)
                 :blueprint
                 :tiles
                 (map :position)
                 set))

         :full #{} #{{:x 239, :y -150}
                     {:x 240, :y -150}
                     {:x 241, :y -150}
                     {:x 242, :y -150}
                     {:x 243, :y -150}
                     {:x 244, :y -150}}
         :full #{"stone-wall" "gate"} #{}
         :sparse #{} #{{:x 239, :y -150}
                       {:x 240, :y -150}
                       {:x 241, :y -150}
                       {:x 242, :y -150}
                       {:x 243, :y -150}
                       {:x 244, :y -150}}
         :sparse #{"stone-wall" "gate"} #{}
         :sparse #{"gate"} #{{:x 239, :y -150}
                             {:x 240, :y -150}
                             {:x 243, :y -150}
                             {:x 244, :y -150}})))
