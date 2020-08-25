(ns factorio-blueprint-tools.core
  (:require-macros [factorio-blueprint-tools.macros :as m]) 
  (:require [factorio-blueprint-tools.controller.tile :as tile-controller]
            [factorio-blueprint-tools.controller.mirror :as mirror-controller]
            [factorio-blueprint-tools.upgrade :as upgrade]
            [factorio-blueprint-tools.controller.upgrade :as upgrade-controller]
            [factorio-blueprint-tools.controller.landfill :as landfill-controller]
            [factorio-blueprint-tools.controller.split :as split-controller]
            [factorio-blueprint-tools.controller.debug :as debug-controller]
            [factorio-blueprint-tools.preview :as preview]
            [factorio-blueprint-tools.serialization :as ser]
            [clojure.string :as str]
            [antizer.rum :as ant]
            [rum.core :as rum]
            [citrus.core :as citrus]))

(enable-console-print!)

;;; Components

; Tools

(def ta-no-spellcheck
  {:autoComplete "off"
   :autoCorrect "off"
   :autoCapitalize "off"
   :spellCheck "false"})

(defn alert-error
  [error-message]
  (ant/alert {:message error-message
              :showIcon true
              :type "error"}))

(rum/defc BlueprintInput <
  rum/reactive
  [r controller]
  (ant/form-item {:label "Blueprint string"
                  :help "Copy a blueprint string from Factorio and paste it in this field"}
                 [:div
                  (ant/input-text-area (assoc ta-no-spellcheck
                                              :class "input-blueprint"
                                              :style {:height "10em" :width "calc(100% - 10em - 24px)"}
                                              :value (rum/react (citrus/subscription r [controller :input :encoded]))
                                              :onChange #(citrus/dispatch! r controller :set-blueprint (-> % .-target .-value))
                                              :onFocus #(.select (-> % .-target))))
                  (when-let [blueprint (rum/react (citrus/subscription r [controller :input :blueprint]))]
                    (preview/preview blueprint))]
                 (when-let [error (rum/react (citrus/subscription r [controller :input :error]))]
                   (alert-error (str "Could not load blueprint.  Please make sure to copy and paste the whole string from Factorio. (Error: " error ")")))))

(rum/defc BlueprintOutput <
  rum/reactive
  [r controller]
  (ant/form-item {:label "Result"
                  :help "Copy this blueprint string and import in from the blueprint library in Factorio"}
                 [:div
                  (ant/input-text-area (assoc ta-no-spellcheck
                                              :class "input-result-blueprint"
                                              :style {:height "10em" :width "calc(100% - 10em - 24px)"}
                                              :value (rum/react (citrus/subscription r [controller :output :encoded]))
                                              :onFocus #(.select (-> % .-target))))
                  (when-let [blueprint (rum/react (citrus/subscription r [controller :output :blueprint]))]
                    (when (:blueprint blueprint)
                      (preview/preview blueprint)))]))

; About

(rum/defc ContentAbout < rum/static
  []
  (ant/layout-content
   {:style {:padding "1ex 1em"}}
   [:div
    (ant/alert {:message "For blueprints from Factorio >= 0.18.37, tick 'Grid size' and 'Absolute reference point'"
                :description "This is bascially the behaviour of previous versions. Otherwise the blueprints are way off the center.  The problem is under investigation."
                :showIcon true
                :type "warning"})]
   [:div {:dangerouslySetInnerHTML {:__html (m/load-markdown "docs.md")}}]
   [:div {:dangerouslySetInnerHTML {:__html (m/load-markdown "changelog.md")}}]))


; Settings 

(rum/defc ContentSettings < rum/static
  []
  (ant/layout-content
   {:style {:padding "1ex 1em"}}
   [:h2 "Settings"]
   (ant/alert {:message "Currently there is no way to change or add mods etc. for the sizes occupied by the entities."
               :showIcon true
               :type "warning"})
   (ant/form
    (ant/form-item {:label "Factorio entities"}
                   (ant/select {:value "vanilla-1.0"}
                               (ant/select-option {:key "vanilla-1.0"} "Vanilla 1.0"))))))

; Tile

(rum/defc ContentTile <
  rum/reactive
  [r]
  (ant/layout-content
   {:style {:padding "1ex 1em"}}
   [:h2 "Tile a blueprint"]
   (ant/form
    (BlueprintInput r :tile))
   (when (rum/react (citrus/subscription r [:tile :input :blueprint]))
     [:div
      (ant/form
       (ant/form-item {:label "Tiles on X axis"}
                      (ant/input-number {:class "input-tile-x"
                                         :value (rum/react (citrus/subscription r [:tile :config :tile-x]))
                                         :onChange #(citrus/dispatch! r :tile :set-config :tile-x %)
                                         :min 1}))
       (ant/form-item {:label "Tiles on Y axis"}
                      (ant/input-number {:class "input-tile-y"
                                         :value (rum/react (citrus/subscription r [:tile :config :tile-y]))
                                         :onChange #(citrus/dispatch! r :tile :set-config :tile-y %)
                                         :min 1}))
       (BlueprintOutput r :tile))])))

; Mirror

(rum/defc ContentMirror <
  rum/reactive
  [r]
  (ant/layout-content
   {:style {:padding "1ex 1em"}}
   [:h2 "Mirror a blueprint"]
   (ant/form
    (BlueprintInput r :mirror))
   (when (rum/react (citrus/subscription r [:mirror :input :blueprint]))
     [:div
      (ant/form
       (ant/form-item {:label "Direction"}
                      (ant/radio-group {:class "input-mirror-direction"
                                        :value (rum/react (citrus/subscription r [:mirror :config :direction]))
                                        :onChange #(citrus/dispatch! r :mirror :set-config :direction (-> % .-target .-value keyword))}
                                       (for [[option label] [[:vertically "Vertically"] [:horizontally "Horizontally"]]]
                                         (ant/radio {:key option :value option} label))))

       (BlueprintOutput r :mirror))])))

; Upgrade

(rum/defc ContentUpgrade <
  rum/reactive
  [r]
  (ant/layout-content
   {:style {:padding "1ex 1em"}}
   [:h2 "Upgrade (or downgrade) a blueprint"]
   (ant/form
    (BlueprintInput r :upgrade))
   (when-let [blueprint (rum/react (citrus/subscription r [:upgrade :input :blueprint]))]
     [:div
      (let [upgradable (upgrade/upgradeable-from-blueprint blueprint)
            order (filter upgradable upgrade/upgrades-order)]
        (ant/form
         (for [from order]
           (ant/form-item {:label (upgrade/upgrades-names from)}
                          (ant/radio-group {:value (rum/react (citrus/subscription r [:upgrade :config from]))
                                            :onChange #(citrus/dispatch! r :upgrade :set-config from (-> % .-target .-value))}
                                           (for [option (upgrade/upgrades-by-key from)]
                                             (ant/radio {:key option :value option} (upgrade/upgrades-names option))))))
         (BlueprintOutput r :upgrade)))])))

; Landfill

(rum/defc ContentLandfill <
  rum/reactive
  [r]
  (ant/layout-content
   {:style {:padding "1ex 1em"}}
   [:h2 "Add landfill as tiles under a blueprint"]
   (ant/alert {:message "Please note, that the modified blueprint can not be placed in one go in Factorio right now.  If there are entities on water, they can not be placed.  Force-place (shift) the blueprint to build the landfill and all placeable entities first, and once the landfill is in, place the blueprint again."
               :showIcon true
               :type "warning"})
   (ant/form
    (BlueprintInput r :landfill))
   (when (rum/react (citrus/subscription r [:landfill :input :blueprint]))
     [:div
      (ant/form
       (BlueprintOutput r :landfill))])))

; Split

(rum/defc ContentSplit <
  rum/reactive
  [r]
  (ant/layout-content
   {:style {:padding "1ex 1em"}}
   [:h2 "Splits a blueprint into multiple tiles"]
   (ant/form
    (BlueprintInput r :split))
   (when (rum/react (citrus/subscription r [:split :input :blueprint]))
     [:div
      (ant/form
       (ant/form-item {:label "Size of one tile"}
                      (ant/input-number {:class "input-split-tile-size"
                                         :value (rum/react (citrus/subscription r [:split :config :tile-size]))
                                         :onChange #(citrus/dispatch! r :split :set-config :tile-size %)
                                         :min 32}))
       (BlueprintOutput r :split))])))

; Debug

(rum/defc ContentDebug <
  rum/reactive
  [r]
  (ant/layout-content
    {:style {:padding "1ex 1em"}}
    [:h2 "Show the content of a blueprint"]
    (ant/form
      (BlueprintInput r :debug))
    (when (rum/react (citrus/subscription r [:debug :input :blueprint]))
      (ant/form-item {:label "EDN"}
                     [:div
                      (ant/input-text-area (assoc ta-no-spellcheck
                                                  :class "input-result-blueprint"
                                                  :style {:height "10em" :width "100%"}
                                                  :value (rum/react (citrus/subscription r [:debug :output]))
                                                  :onFocus #(.select (-> % .-target))))]))))

;;; Main

; Navigation

(defmulti navigation identity)

(defmethod navigation :init []
  (let [navigations [{:key "about" :icon "info-circle-o" :title "About" :component ContentAbout}
                     {:key "tile" :icon "appstore-o" :title "Tile" :component ContentTile}
                     {:key "split" :icon "scissor" :title "Split" :component ContentSplit}
                     {:key "mirror" :icon "swap" :title "Mirror" :component ContentMirror}
                     {:key "upgrade" :icon "tool" :title "Upgrade" :component ContentUpgrade}
                     {:key "landfill" :icon "table" :title "Landfill" :component ContentLandfill}
                     {:key "debug" :icon "bug" :title "Debug" :component ContentDebug}
                     {:key "settings " :icon "setting" :title "Settings" :component ContentSettings}]
        navigations-by-key (into {} (map (juxt :key identity)) navigations)]
    {:state {:current (-> navigations first :key)
             :navigations navigations
             :navigations-by-key navigations-by-key}}))

(defmethod navigation :goto [_ [target] state]
  {:state (assoc state :current target)})


;; Effect Handlers

(defn dispatch [r _ events]
  (doseq [[ctrl & args] events]
    (apply citrus/dispatch! (into [r ctrl] args))))


;; Reconciler

(defonce reconciler
  (citrus/reconciler
   {:state (atom {})
    :controllers {:navigation navigation
                  :tile tile-controller/tile
                  :mirror mirror-controller/mirror
                  :upgrade upgrade-controller/upgrade
                  :landfill landfill-controller/landfill
                  :split split-controller/split 
                  :debug debug-controller/debug}
    :effect-handlers {:dispatch dispatch}}))

;;; Main content

(defn- menu-item
  [{:keys [key icon title]}]
  (ant/menu-item {:key key :class (str "menu-" key)} [:span (ant/icon {:type icon}) title]))

(rum/defc AppHeader < rum/static []
  (ant/layout-header
   {:style {:padding-left "24px"}}
   [:h1
    {:style {:color "white"}}
    (ant/icon {:type "setting"})
    "Factorio Blueprint Tools"]))

(rum/defc AppFooter < rum/static []
  (ant/layout-footer
   {:style {:text-align "center"}}
   [:span
    "Copyright © 2020 Christoph Frick"
    " "
    [:a {:href "https://github.com/christoph-frick/factorio-blueprint-tools"} "https://github.com/christoph-frick/factorio-blueprint-tools"]]))

(rum/defc App < rum/reactive
  [r]
  (ant/layout {:style {:min-height "100vh"}}
              (AppHeader)
              (let [{:keys [current navigations navigations-by-key]} (rum/react (citrus/subscription r [:navigation]))]
                (ant/layout
                 (ant/layout-sider
                  {:theme "light"}
                  (ant/menu {:theme "light"
                             :mode "inline"
                             :selectedKeys [current]
                             :onSelect #(citrus/dispatch! r :navigation :goto (.-key %))
                             :style {:min-height "calc(100vh-64px)"}}
                            (map menu-item navigations)))
                 (ant/layout
                  (if-let [navigation (navigations-by-key current)]
                    ((:component navigation) r)
                    (do
                      (ContentAbout r)
                      (ant/message-error (str "Unknown navigation target: " current))))
                  (AppFooter))))))

(defonce init-ctrl
  (citrus/broadcast-sync! reconciler :init))

(defn init!
  []
  (rum/mount (App reconciler) (js/document.getElementById "app")))

(init!)

(defn on-js-reload [])
