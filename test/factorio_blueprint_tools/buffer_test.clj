(ns factorio-blueprint-tools.buffer-test
  (:require [clojure.test :refer [deftest testing is are]]
            [factorio-blueprint-tools.buffer :as sut]))

(def blueprint
  {:blueprint {:entities [{:entity_number 1, :direction 0, :name "transport-belt", :position {:x 0.5, :y 1.5}}
                          {:entity_number 2, :direction 0, :name "transport-belt", :position {:x 0.5, :y 0.5}}
                          {:entity_number 3, :direction 0, :name "transport-belt", :position {:x 0.5, :y 3.5}}
                          {:entity_number 4, :direction 0, :name "transport-belt", :position {:x 0.5, :y 2.5}}
                          {:entity_number 5, :direction 0, :name "transport-belt", :position {:x 1.5, :y 4.5}}
                          {:entity_number 6, :direction 0, :name "transport-belt", :position {:x 0.5, :y 4.5}}
                          {:entity_number 7, :direction 0, :name "transport-belt", :position {:x 3.5, :y 4.5}}
                          {:entity_number 8, :direction 0, :name "transport-belt", :position {:x 2.5, :y 4.5}}]}})

(deftest test-histogram
  (is (= {"transport-belt" 8} (sut/histogram blueprint))))

(deftest test-next-request-filter
  (are [histogram' count histogram] (= [histogram' count] (sut/next-request-filter histogram "transport-belt"))
    {} 5 {"transport-belt" 5}
    {"transport-belt" 1} 100 {"transport-belt" 101}))

(deftest test-request-filters-from-histogram
  (is (= [{"transport-belt" 8}]
         (-> blueprint sut/histogram sut/request-filters-from-histogram))))

(def large-histogram
  (into {} (map #(vector (str %) 10)) (range (inc sut/buffer-chest-slots))))

(deftest test-request-filters
  (are [result histogram] (= result (sut/request-filters-from-histogram histogram))

    [{"transport-belt" 5}]
    {"transport-belt" 5}

    [{"transport-belt" 100}
     {"transport-belt" 5}]
    {"transport-belt" 105}

    [(into {} (take sut/buffer-chest-slots large-histogram))
     (into {} (drop sut/buffer-chest-slots large-histogram))]
    large-histogram))
