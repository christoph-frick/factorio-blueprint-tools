(ns factorio-blueprint-tools.serialization-test
  (:require [clojure.test :refer [deftest testing is are]]
            [factorio-blueprint-tools.serialization :as sut]))

(deftest test-serde-roundtrip
  (let [blueprint {"blueprint" {"entities" [{"direction" 2,
                                             "entity_number" 1,
                                             "name" "transport-belt",
                                             "position" {"x" -1, "y" -1}}
                                            {"direction" 2,
                                             "entity_number" 2,
                                             "name" "transport-belt",
                                             "position" {"x" 0, "y" 0}}],
                                "icons" [{"index" 1, "signal" {"name" "transport-belt", "type" "item"}}],
                                "item" "blueprint",
                                "tiles" [{"name" "stone-path", "position" {"x" -1, "y" -1}}
                                         {"name" "stone-path", "position" {"x" -1, "y" 0}}
                                         {"name" "stone-path", "position" {"x" 0, "y" -1}}
                                         {"name" "stone-path", "position" {"x" 0, "y" 0}}],
                                "version" 73017655298}}
        blueprint-string "0eJyVkN0KwjAMhd8l1x3Uyfzpq4jINoMGurS0URyl7+7+LrwR51VCcvJxchI09oE+EAuYBMhCQhjBnBJcKWAr5BhMqeZVf+FH12AAs1HAdYdgQELN0bsgRYNWQIF3keazBC8wxSDtx5Kz+gEtV0P1xNQ5nxVQ63h2THzF1+Qt0o1rO4q/AaX345wEO5gxY2c+8hg0ZJcsFkoUx1j4Wu6//lx/oNfq9Z/8z4yeGOK02W/1Zr+rqvJ4yPkN6UeneA=="]
    (is (= blueprint (-> blueprint sut/encode sut/decode)))
    (is (= blueprint-string (sut/encode blueprint)))
    (is (= blueprint (sut/decode blueprint-string)))))
