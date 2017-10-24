(ns factorio-blueprint-tools.core
  (:require [factorio-blueprint-tools.tile :refer [tile]]
            [factorio-blueprint-tools.serialization :as ser]
            [antizer.rum :as ant]
            [rum.core :as rum]))

(enable-console-print!)

(defonce navigation-state
  (atom "about"))

(def ta-no-spellcheck
  {:autoComplete "off"
   :autoCorrect "off"
   :autoCapitalize "off"
   :spellCheck "false"})

(rum/defc content-about < rum/static
  []
  (ant/layout-content
   {:style {:padding "1ex 1em"}}
   [:h1 "Factorio Blueprint Tools"]
   [:p "Random tools to manipulate Factorio blueprint strings like tiling."]))

(rum/defc content-settings < rum/static
  []
  (ant/layout-content
   {:style {:padding "1ex 1em"}}
   [:h1 "Settings"]
   (ant/alert {:message "Currenlty there is no way to change or add mods etc. for the sizes occupied by the entities."
               :showIcon true
               :type "warning"
               })
   (ant/form
    (ant/form-item {:label "Factorio entities"}
                   (ant/select {:value "vanilla-0.15"}
                    (ant/select-option {:key "vanilla-0.15"} "Vanilla 0.15"))))))

(defn- save-decode-blueprint-string
  [blueprint-string]
  (try
    (ser/decode blueprint-string)
    (catch :default e
      (ant/message-error (str "Could not load blueprint.  Please make sure to copy and paste the whole string from Factorio. (Error: " e ")"))
      nil)))

(defn- do-tile
  [blueprint-string x-times y-times]
  (when (seq blueprint-string)
    (some-> blueprint-string
            (save-decode-blueprint-string)
            (tile x-times y-times)
            (ser/encode))))

(rum/defcs content-tile <
  (rum/local "" ::blueprint-string)
  (rum/local 2 ::tile-x)
  (rum/local 2 ::tile-y)
  [state]
  (let [tile-result (rum/derived-atom [(::blueprint-string state) (::tile-x state) (::tile-y state)] ::tile-result
                                      (fn [blueprint-string tile-x tile-y]
                                        (do-tile blueprint-string tile-x tile-y)))]
    (ant/layout-content
     {:style {:padding "1ex 1em"}}
     [:h1 "Tile a blueprint N x M times"]
     (ant/form
      (ant/form-item {:label "Paste your blueprint string"}
                     (ant/input-text-area (assoc ta-no-spellcheck
                                                 :value @(::blueprint-string state)
                                                 :onChange #(reset! (::blueprint-string state) (-> % .-target .-value))
                                                 :onFocus #(.select (-> % .-target)))))
      (let [blueprint-string @(::blueprint-string state)]
        (when (seq blueprint-string)
          (when-let [blueprint (save-decode-blueprint-string blueprint-string)]
            (ant/form
             (ant/form-item {:label "Tile n times on the X axis"}
                            (ant/input-number {:value @(::tile-x state)
                                               :onChange #(reset! (::tile-x state) %)
                                               :min 2}))
             (ant/form-item {:label "Tile m times on the Y axis"}
                            (ant/input-number {:value @(::tile-y state)
                                               :onChange #(reset! (::tile-y state) %)
                                               :min 2}))
             (ant/form-item {:label "Result"}
                            (ant/input-text-area (assoc ta-no-spellcheck
                                                        :value @tile-result
                                                        :onFocus #(.select (-> % .-target)))))))))))))

(rum/defc render < rum/reactive
  []
  (ant/layout {:style {:min-height "100vh"}}
              (ant/layout-sider
               (ant/menu {:theme "dark"
                          :mode "inline"
                          :selectedKeys [(rum/react navigation-state)]
                          :onSelect #(reset! navigation-state (.-key %))
                          :style {:line-height "64px"}}
                         (ant/menu-item {:key "about"} [:span (ant/icon {:type "info-circle-o"}) "About"])
                         (ant/menu-item {:key "tile"} [:span (ant/icon {:type "appstore-o"}) "Tile"])
                         (ant/menu-item {:key "settings"} [:span (ant/icon {:type "setting"}) "Settings (Vanilla 0.15)"])))
              (ant/layout
               (case (rum/react navigation-state)
                 "tile" (content-tile)
                 "about" (content-about)
                 "settings" (content-settings))
               (ant/layout-footer
                [:span
                 "Copyright © 2017 Christoph Frick"
                 " "
                 [:a {:href "https://github.com/christoph-frick/factorio-blueprint-tools"} "https://github.com/christoph-frick/factorio-blueprint-tools"]]))))

(defn init!
  []
  (rum/mount (render) (js/document.getElementById "app")))

(init!)

(defn on-js-reload [])
