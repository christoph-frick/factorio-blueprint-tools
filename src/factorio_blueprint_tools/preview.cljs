(ns factorio-blueprint-tools.preview
  (:require [factorio-blueprint-tools.blueprint :as blueprint]
            [goog.string]
            [goog.color]
            #_:clj-kondo/ignore
            [hiccups.runtime :as hiccupsrt])
  (:require-macros [hiccups.core :as hiccups :refer [html]]))

(defn color-by-entity-name
  [entity-name]
  (as-> entity-name x
    (.hashCode goog.string x)
    (.toString x 16)
    (str x "000000") ; pad with enough zeros to work for sure; see #9
    (.substring x 0 6)
    (.concat "#" x)
    (.hexToRgb goog.color x)
    (.lighten goog.color x 0.5)
    (.rgbArrayToHex goog.color x)))

(def color-by-entity-name-memoized
  (memoize color-by-entity-name))

(defn preview
  [blueprint]
  (let [entities (blueprint/entities blueprint)
        tiles (blueprint/tiles blueprint)
        [[ax1 ay1] [ax2 ay2]] (blueprint/blueprint-area blueprint)]
    (html
     [:svg
      {:width "10em"
       :height "10em"
       :viewBox (str ax1 " " ay1 " " (- ax2 ax1) " " (- ay2 ay1))
       :style "vertical-align: top; outline: 1px solid #d9d9d9; background-color: #fff"}
      (when (seq tiles)
        [:g {:stroke "white" :stroke-width "0.05%" :opacity "50%"}
         (for [t tiles
               :let [[x y] (blueprint/entity-coord t)]]
           [:g
            [:rect {:fill (color-by-entity-name-memoized (:name t)) :x x :y y :width 1 :height 1}]])])

      (when (seq entities)
        [:g
         (for [e entities
               :let [[[x1 y1] [x2 y2]] (blueprint/entity-area e)
                     width (- x2 x1)
                     height (- y2 y1)]]
           [:g {:stroke "black" :stroke-width "0.5%"}
            [:title (:name e)]
            [:rect {:fill (color-by-entity-name-memoized (:name e)) :x x1 :y y1 :width width :height height}]
            [:path {:d "M 0,-0.1 -0.1,0 0.1,0 z" :transform (str "translate(" (-> e :position :x) "," (-> e :position :y) ")" "rotate(" (* 45 (:direction e 0)) ")")}]])])

      (when (blueprint/absolute-snapping? blueprint)
        (let [[width height] (blueprint/snap-grid blueprint)]
          [:g {:stroke "lightgreen" :stroke-width "5%" :stroke-dasharray "0.33"}
           [:rect {:fill "none" :x 0 :y 0 :width width :height height}]]))])))
