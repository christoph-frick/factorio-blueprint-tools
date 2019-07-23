(ns factorio-blueprint-tools.coord-test
  (:require [clojure.test :refer [deftest is are]]
            [factorio-blueprint-tools.coord :as t]))

(deftest test-union-box
  (is (= (t/box t/-ONE t/ONE)
         (t/union-box (t/box t/-ONE t/ZERO) (t/box t/ZERO t/ONE)))))

(deftest test-in-coord?
  (are [v ?] (= ? (t/in-coord? (t/coord 0 1) v))
    -1 false
    0 true
    0.5 true
    1 false
    2 false))
