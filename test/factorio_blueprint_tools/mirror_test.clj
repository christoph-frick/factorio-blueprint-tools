(ns factorio-blueprint-tools.mirror-test
  (:require [clojure.test :refer [deftest testing is are]]
            [factorio-blueprint-tools.mirror :as t]))

(deftest test-mirror-tiles-remain-under-items
  (let [orig {:blueprint {:tiles [{:name "stone-path", :position {:x 58, :y -9}}
                                  {:name "concrete", :position {:x 61, :y -9}}]}}
        target {:blueprint {:tiles [{:name "stone-path", :position {:x 61, :y -9}}
                                    {:name "concrete", :position {:x 58, :y -9}}]}}]
    (is (= (t/mirror orig :vertically) target))
    (is (= (t/mirror (t/mirror orig :vertically) :vertically) orig))))
