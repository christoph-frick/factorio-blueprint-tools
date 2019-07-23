(ns factorio-blueprint-tools.sizes-test
  (:require [clojure.test :refer [deftest is are]]
            [factorio-blueprint-tools.sizes :as t]))

(deftest test-unknown-id-has-default
  (is (= (t/selection-box :does-not-exist) t/selection-box-fallback)))
