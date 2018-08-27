(ns factorio-blueprint-tools.preview
  (:require [factorio-blueprint-tools.blueprint :as blueprint]
            [goog.string]
            [goog.color]))

(defn color-by-entity-name
  [entity-name]
  (as-> entity-name x
    (.hashCode goog.string x)
    (.toString x 16)
    (.substring x 0 6)
    (.concat "#" x)
    (.hexToRgb goog.color x)
    (.lighten goog.color x 0.5)
    (.rgbArrayToHex goog.color x)))

(defn preview
  [blueprint]
  (let [entities (-> blueprint :blueprint :entities)
        [[ax1 ay1] [ax2 ay2]] (blueprint/entities-area blueprint)]
    [:svg
     {:width "20em" :height "20em" :viewBox (str ax1 " " ay1 " " (- ax2 ax1) " " (- ay2 ay1)) :style {:outline "1px solid black" :background-color "#fff"}}
     [:g
      (for [e entities
            :let [[[x1 y1] [x2 y2]] (blueprint/entity-area e)
                  width (- x2 x1)
                  height (- y2 y1)]]
        [:g {:stroke "black" :stroke-width "0.5%"}
         [:rect {:fill (color-by-entity-name (:name e)) :x x1 :y y1 :width width :height height}]
         [:path {:d "M 0,-0.1 -0.1,0 0.1,0 z" :transform (str "translate(" (-> e :position :x) "," (-> e :position :y) ")" "rotate(" (* 45 (:direction e 0)) ")")}]])]]))
