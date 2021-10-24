(ns factorio-blueprint-tools.core
  (:require-macros [factorio-blueprint-tools.macros :as m])
  (:require [factorio-blueprint-tools.controller.tile :as tile-controller]
            [factorio-blueprint-tools.controller.mirror :as mirror-controller]
            [factorio-blueprint-tools.upgrade :as upgrade]
            [factorio-blueprint-tools.controller.upgrade :as upgrade-controller]
            [factorio-blueprint-tools.controller.landfill :as landfill-controller]
            [factorio-blueprint-tools.controller.split :as split-controller]
            [factorio-blueprint-tools.controller.buffer :as buffer-controller]
            [factorio-blueprint-tools.controller.debug :as debug-controller]
            [factorio-blueprint-tools.preview :as preview]
            [clojure.string :as str]
            [cljs.pprint]
            [antizer.rum :as ant]
            [rum.core :as rum]
            [citrus.core :as citrus]
            [pushy.core :as pushy]))

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

(defn radio-options
  [options]
  (for [[option label help] options]
    (ant/radio
     {:key option
      :value option}
     [:span label
      " "
      [:span {:class "ant-form-explain"} help]])))

(rum/defc BlueprintPreview < rum/static
  [blueprint]
  [:span
   {:style {:padding-left "24px"}
    :dangerouslySetInnerHTML {:__html (preview/preview blueprint)}}])

(rum/defc BlueprintInput <
  rum/reactive
  [r controller]
  (ant/form-item {:label "Blueprint string"
                  :help "Copy a blueprint string from Factorio and paste it in this field"}
                 [:div
                  (ant/input-text-area (assoc ta-no-spellcheck
                                              :class "input-blueprint"
                                              :allow-clear true
                                              :style {:height "10em" :width "calc(100% - 10em - 24px)"}
                                              :value (rum/react (citrus/subscription r [controller :input :encoded]))
                                              :onChange #(citrus/dispatch! r controller :set-blueprint (-> % .-target .-value))
                                              :onFocus #(.select (.-target %))))
                  (when-let [blueprint (rum/react (citrus/subscription r [controller :input :blueprint]))]
                    (BlueprintPreview blueprint))
                  (when-let [error (rum/react (citrus/subscription r [controller :input :error]))]
                    (alert-error (str "Could not load blueprint.  Please make sure to copy and paste the whole string from Factorio. (Error: " error ")")))]))

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
                                              :onFocus #(.select (.-target %))))
                  (when-let [blueprint (rum/react (citrus/subscription r [controller :output :blueprint]))]
                    (when (:blueprint blueprint)
                      (BlueprintPreview blueprint)))]))

; About

(rum/defc ContentAbout < rum/static
  []
  (ant/layout-content
   {:class "content"}
   [:div {:dangerouslySetInnerHTML {:__html (m/load-markdown "docs.md")}}]
   [:div
    [:h2 "Reporting Bugs"]
    "In case you find a bug or wish for a feature, feel free to " [:a {:href "https://github.com/christoph-frick/factorio-blueprint-tools/issues"} "create an issue"] "."
    " "
    "It is super helpful to include how to reproduce the bug e.g. by providing a blueprint string."]
   [:div {:dangerouslySetInnerHTML {:__html (m/load-markdown "changelog.md")}}]))


; Settings

(rum/defc ContentSettings < rum/static
  []
  (ant/layout-content
   {:class "content"}
   [:h2 "Settings"]
   (ant/alert {:message "Currently there is no way to change or add mods etc. for the sizes occupied by the entities."
               :showIcon true
               :type "warning"})
   (ant/form
    (ant/form-item {:label "Factorio entities"}
                   (ant/select {:value "vanilla-1.0"}
                               (ant/select-option {:key "vanilla-1.0"} "Vanilla 1.1"))))))

; Tile

(rum/defc ContentTile <
  rum/reactive
  [r]
  (ant/layout-content
   {:class "content"}
   [:h2 "Tile a blueprint"]
   [:p "Arrange copies of the blueprint in a grid.  E.g. take a six electric miner blueprint and tile 15x15 to cover even the biggest resource fields" ]
   (ant/form
    (BlueprintInput r :tile))
   (when (rum/react (citrus/subscription r [:tile :input :blueprint]))
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
      (BlueprintOutput r :tile)))))

; Mirror

(rum/defc ContentMirror <
  rum/reactive
  [r]
  (ant/layout-content
   {:class "content"}
   [:h2 "Mirror a blueprint"]
   [:p "Mirror the blueprint either vertically or horizontally"]
   (ant/form
    (BlueprintInput r :mirror))
   (when (rum/react (citrus/subscription r [:mirror :input :blueprint]))
     (ant/form
      (ant/form-item {:label "Direction"}
                     (ant/radio-group {:class "input-mirror-direction"
                                       :value (rum/react (citrus/subscription r [:mirror :config :direction]))
                                       :onChange #(citrus/dispatch! r :mirror :set-config :direction (-> % .-target .-value keyword))}
                                      (for [[option label] [[:vertically "Vertically"] [:horizontally "Horizontally"]]]
                                        (ant/radio {:key option :value option} label))))

      (BlueprintOutput r :mirror)))))

; Upgrade

(rum/defc ContentUpgrade <
  rum/reactive
  [r]
  (ant/layout-content
   {:class "content"}
   [:h2 "Upgrade (or downgrade) a blueprint"]
   [:p "Decide what common upgradeable entities (e.g. inserters) to upgrade.  Also supports downgrading (e.g. you have a great blueprint but not the tech yet)"]
   (ant/form
    (BlueprintInput r :upgrade))
   (when-let [blueprint (rum/react (citrus/subscription r [:upgrade :input :blueprint]))]
     (let [upgradable (upgrade/upgradeable-from-blueprint blueprint)
           order (filter upgradable upgrade/upgrades-order)]
       (ant/form
        (for [from order]
          (ant/form-item {:label (upgrade/upgrades-names from)}
                         (ant/radio-group {:value (rum/react (citrus/subscription r [:upgrade :config from]))
                                           :onChange #(citrus/dispatch! r :upgrade :set-config from (-> % .-target .-value))}
                                          (for [option (upgrade/upgrades-by-key from)]
                                            (ant/radio {:key option :value option} (upgrade/upgrades-names option))))))
        (BlueprintOutput r :upgrade))))))

; Landfill

(rum/defc ContentLandfill <
  rum/reactive
  [r]
  (ant/layout-content
   {:class "content"}
   [:h2 "Add landfill as tiles under a blueprint"]
   [:p "Put landfill under a blueprint"]
   (ant/alert {:message "Please note, that the modified blueprint can not be placed in one go in Factorio right now.  If there are entities on water, they can not be placed.  Force-place (shift) the blueprint to build the landfill and all placeable entities first, and once the landfill is in, place the blueprint again."
               :showIcon true
               :type "warning"})
   (ant/form
    (BlueprintInput r :landfill))
   (when (rum/react (citrus/subscription r [:landfill :input :blueprint]))
     (ant/form
      (ant/form-item {:label "Filling mode"}
                     (ant/radio-group {:class "input-landfill-fill-mode"
                                       :value (rum/react (citrus/subscription r [:landfill :config :fill-mode]))
                                       :onChange #(citrus/dispatch! r :landfill :set-config :fill-mode (-> % .-target .-value keyword))}
                                      (radio-options [[:full "Full" "(complete area/bounding box of blueprint)"]
                                                      [:sparse "Sparse" "(only under entities; keeps gap for pumps)"]])))
      (ant/form-item {:label "Existing tiles"}
                     (ant/radio-group {:class "input-landfill-tile-mode"
                                       :value (rum/react (citrus/subscription r [:landfill :config :tile-mode]))
                                       :onChange #(citrus/dispatch! r :landfill :set-config :tile-mode (-> % .-target .-value keyword))}
                                      (radio-options [[:remove "Remove" "(all tiles are removed)"]
                                                      [:replace "Replace" "(tiles are removed, but landfill is also added where tiles where honouring the filling mode)"]
                                                      [:to-book "Blueprint book" "(separate blueprint for landfill and original as book)"]])))
      (BlueprintOutput r :landfill)))))

; Split

(rum/defc ContentSplit <
  rum/reactive
  [r]
  (ant/layout-content
   {:class "content"}
   [:h2 "Splits a blueprint into multiple tiles"]
   [:p "Split a large blueprint into tiles to make it easier to place in game"]
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

; Buffer

(rum/defc ContentBuffer <
  rum/reactive
  [r]
  (ant/layout-content
   {:class "content"}
   [:h2 "Create buffer chests"]
   [:p "Turn a blueprint into a blueprint for buffer chests requesting the initial blueprint"]
   (ant/alert {:message "This is currently under development"
               :showIcon true
               :type "warning"})
   (ant/form
    (BlueprintInput r :buffer))
   (when (rum/react (citrus/subscription r [:buffer :input :blueprint]))
     [:div
      (ant/form
       (BlueprintOutput r :buffer))])))

; Debug

(defn pprint
  [edn]
  (with-out-str
      (cljs.pprint/pprint edn)))

(rum/defc ContentDebug <
  rum/reactive
  [r]
  (ant/layout-content
   {:class "content"}
   [:h2 "Show the content of a blueprint"]
   (ant/form
    (BlueprintInput r :debug)
    (when (rum/react (citrus/subscription r [:debug :input :blueprint]))
      (ant/form-item {:label "EDN"}
                     (ant/input-text-area {:style {:font-family "monospace"}
                                           :auto-size true
                                           :value (pprint (rum/react (citrus/subscription r [:debug :output])))}))))))

;;; Main

; Navigation

(defonce navigations
  [{:key "about"    :icon "info-circle-o" :title "About"        :component ContentAbout}
   {:key "tile"     :icon "appstore-o"    :title "Tile"         :component ContentTile}
   {:key "split"    :icon "scissor"       :title "Split"        :component ContentSplit}
   {:key "mirror"   :icon "swap"          :title "Mirror"       :component ContentMirror}
   {:key "upgrade"  :icon "tool"          :title "Upgrade"      :component ContentUpgrade}
   {:key "landfill" :icon "table"         :title "Landfill"     :component ContentLandfill}
   {:key "buffer"   :icon "filter"        :title "Buffer-Chest" :component ContentBuffer}
   {:key "debug"    :icon "bug"           :title "Debug"        :component ContentDebug}
   {:key "settings" :icon "setting"       :title "Settings"     :component ContentSettings}])

(defn key-to-route
    [key]
    (str "#" key))

(defonce navigations-by-key
  (into {}
        (map (juxt (comp key-to-route :key) identity))
        navigations))

(defonce default-navigation
  (-> navigations first :key key-to-route))

(declare reconciler)

(defn route-to-key
  [route]
  (when-let [idx (some-> route (str/index-of "#"))]
    (when-let [key (subs route idx)]
      (when-let [_ (get navigations-by-key key)]
        key))))

(def history
  (pushy/pushy
   #(citrus/dispatch! reconciler :navigation :goto %) ; not partial!
   (fn [route]
     (if-let [nav-key (route-to-key route)]
       nav-key
       default-navigation))))

(defn nav!
  [nav-key]
  (pushy/set-token! history nav-key))

(defmulti navigation identity)

(defmethod navigation :init []
  {:state {:current default-navigation
           :navigations navigations
           :navigations-by-key navigations-by-key}})

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
                  :buffer buffer-controller/buffer
                  :debug debug-controller/debug}
    :effect-handlers {:dispatch dispatch}}))

;;; Main content

(defn- menu-item
  [{:keys [key icon title]}]
  (ant/menu-item {:key (key-to-route key) :class (str "menu-" key)} [:span (ant/icon {:type icon}) title]))

(rum/defc AppHeader < rum/static []
  (ant/layout-header
   {:style {:padding-left "16px"}}
   [:h1
    {:style {:color "white"}}
    (ant/icon {:type "setting" :style {:padding-right "12px"}})
    "Factorio Blueprint Tools"]))

(rum/defc AppFooter < rum/static []
  (ant/layout-footer
   {:style {:text-align "center"}}
   [:span
    "Copyright © 2021 Christoph Frick"
    " — "
    [:a {:href "https://github.com/christoph-frick/factorio-blueprint-tools"} "Source code"]
    " — "
    [:a {:href "https://github.com/christoph-frick/factorio-blueprint-tools/issues"} "Found an issue?"]]))

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
                             :onSelect #(nav! (.-key %))
                             :style {:min-height "calc(100vh-64px)"}}
                            (map menu-item navigations)))
                 (ant/layout
                    ((:component (navigations-by-key current)) r)
                    (AppFooter))))))

(defonce init-ctrl
  (citrus/broadcast-sync! reconciler :init))

(defn init!
  []
  (rum/mount (App reconciler) (js/document.getElementById "app")))

(init!)
(pushy/start! history)

(defn on-js-reload [])
