(ns factorio-blueprint-tools.blueprint-test
  (:require [clojure.test :as t]
            [factorio-blueprint-tools.blueprint :as sut]))

(t/deftest test-curved-rails-without-direction-19
  (let [entity {:name "curved-rail"
                :position {:x 0 :y 0}
                ; no direction
                }]
    (t/is (= [[-2.5 -4.0] [2.0 4.0]]
             (sut/entity-area entity)))))
