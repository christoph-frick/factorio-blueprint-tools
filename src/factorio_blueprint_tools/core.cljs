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
            [citrus.core :as citrus]
            [pushy.core :as pushy]
            [rum.core :as rum]
            ["antd/es/config-provider" :default ConfigProvider]
            ["antd/es/theme$default" :refer (darkAlgorithm)]
            ["antd/es/alert" :default Alert]
            ["antd/es/radio" :default Radio]
            ["antd/es/select" :default Select]
            ["antd/es/button" :default Button]
            ["antd/es/transfer" :default Transfer]
            ["antd/es/layout" :default Layout]
            ["antd/es/input" :default Input]
            ["antd/es/input-number" :default InputNumber]
            ["antd/es/form" :default Form]
            ["antd/es/menu" :default Menu]
            ["antd/es/typography" :default Typography]
            ["@ant-design/icons" :as Icons]))

(enable-console-print!)

;;; Components

; Tools

(defn alert-error
  [error-message]
  (rum/adapt-class Alert {:message error-message
              :showIcon true
              :type "error"}))

(defn radio-options
  [options]
  (for [[option label help] options]
    (rum/adapt-class Radio
                     {:value (name option)}
                     (let [content [:span (or label option)]]
                       (if help
                         (into content [" "
                                        (rum/adapt-class (.-Text Typography) {:type :secondary} help)])
                         content)))))

(rum/defc BlueprintPreview < rum/static
  [blueprint]
  [:img {:title "Blueprint preview"
         :src (str "data:image/svg+xml,"
                   (js/encodeURIComponent (preview/preview blueprint)))}])

(rum/defc BlueprintInput <
  rum/reactive
  [r controller]
  (rum/adapt-class (.-Item Form) {:label "Blueprint string"
                  :help "Copy a blueprint string from Factorio and paste it in this field"}
                 [:div
                  {:style {:display :flex
                           :gap "1ex"}}
                  (rum/adapt-class (.-TextArea Input) {:autoComplete "off"
                                                       :autoCorrect "off"
                                                       :autoCapitalize "off"
                                                       :spellCheck "false"
                                                       :className "input-blueprint"
                                                       :allowClear true
                                                       :style {:height "160px"
                                                               :flexGrow 1}
                                                       :value (rum/react (citrus/subscription r [controller :input :encoded]))
                                                       :onChange #(citrus/dispatch! r controller :set-blueprint (-> % .-target .-value))
                                                       :onFocus #(.select (.-target %))})
                  (when-let [blueprint (rum/react (citrus/subscription r [controller :input :blueprint]))]
                    (BlueprintPreview blueprint))
                  (when-let [error (rum/react (citrus/subscription r [controller :input :error]))]
                    (alert-error (str "Could not load blueprint.  Please make sure to copy and paste the whole string from Factorio. (Error: " error ")")))]))

(rum/defc BlueprintOutput <
  rum/reactive
  [r controller]
  (rum/adapt-class (.-Item Form) {:label "Result"
                  :help "Copy this blueprint string and import in from the blueprint library in Factorio"}
                 [:div
                  {:style {:display :flex
                           :gap "1ex"}}
                  (rum/adapt-class (.-TextArea Input) {:autoComplete "off"
                                                       :autoCorrect "off"
                                                       :autoCapitalize "off"
                                                       :spellCheck "false"
                                                       :className "input-result-blueprint"
                                                       :style {:height "160px"
                                                               :flexGrow 1}
                                                       :value (rum/react (citrus/subscription r [controller :output :encoded]))
                                                       :onFocus #(.select (.-target %))})
                  (when-let [blueprint (rum/react (citrus/subscription r [controller :output :blueprint]))]
                    (when (:blueprint blueprint)
                      (BlueprintPreview blueprint)))]))

; About

(rum/defc ContentAbout < rum/static
  []
  (rum/adapt-class (.-Content Layout)
   {:className "content"}
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
  (rum/adapt-class (.-Content Layout)
   {:className "content"}
   [:h2 "Settings"]
   (rum/adapt-class Alert {:message "Currently there is no way to change or add mods etc. for the sizes occupied by the entities."
               :showIcon true
               :type "warning"})
   (rum/adapt-class Form
                    {:layout :vertical}
    (rum/adapt-class (.-Item Form) {:label "Factorio entities"}
                   (rum/adapt-class Select {:value "vanilla-1.0"
                                            :options (array #js {:value "vanilla-1.0"
                                                                 :label  "Vanilla 1.1"})})))))

; Tile

(rum/defc ContentTile <
  rum/reactive
  [r]
  (rum/adapt-class (.-Content Layout)
   {:className "content"}
   [:h2 "Tile a blueprint"]
   [:p "Arrange copies of the blueprint in a grid.  E.g. take a six electric miner blueprint and tile 15x15 to cover even the biggest resource fields" ]
   (rum/adapt-class Form
                    {:layout :vertical}
    (BlueprintInput r :tile))
   (when (rum/react (citrus/subscription r [:tile :input :blueprint]))
     (rum/adapt-class Form
                      {:layout :vertical}
      (rum/adapt-class (.-Item Form) {:label "Tiles on X/Y axis"}
                     (rum/adapt-class InputNumber {:className "input-tile-x"
                                        :value (rum/react (citrus/subscription r [:tile :config :x-times]))
                                        :onChange #(citrus/dispatch! r :tile :set-config :x-times %)
                                        :min 1})
                     " X "
                     (rum/adapt-class InputNumber {:className "input-tile-y"
                                        :value (rum/react (citrus/subscription r [:tile :config :y-times]))
                                        :onChange #(citrus/dispatch! r :tile :set-config :y-times %)
                                        :min 1}))
      (rum/adapt-class (.-Item Form) {:label "Gap between entities on X/Y axis"}
                     (rum/adapt-class InputNumber {:className "input-offset-x"
                                        :value (rum/react (citrus/subscription r [:tile :config :x-offset]))
                                        :onChange #(citrus/dispatch! r :tile :set-config :x-offset %)
                                        :min 0})
                     " X "
                     (rum/adapt-class InputNumber {:className "input-offset-y"
                                        :value (rum/react (citrus/subscription r [:tile :config :y-offset]))
                                        :onChange #(citrus/dispatch! r :tile :set-config :y-offset %)
                                        :min 0}))
      (BlueprintOutput r :tile)))))

; Mirror

(rum/defc ContentMirror <
  rum/reactive
  [r]
  (rum/adapt-class (.-Content Layout)
   {:className "content"}
   [:h2 "Mirror a blueprint"]
   [:p "Mirror the blueprint either vertically or horizontally"]
   (rum/adapt-class Form
                    {:layout :vertical}
    (BlueprintInput r :mirror))
   (when (rum/react (citrus/subscription r [:mirror :input :blueprint]))
     (rum/adapt-class Form
                      {:layout :vertical}
                      (rum/adapt-class (.-Item Form) {:label "Direction"}
                                       (rum/adapt-class (.-Group Radio) {:className "input-mirror-direction"
                                                               :value (rum/react (citrus/subscription r [:mirror :config :direction] name))
                                                               :onChange #(citrus/dispatch! r :mirror :set-config :direction (-> % .-target .-value keyword))}
                                                        (radio-options [[:vertically "Vertically"]
                                                                        [:horizontally "Horizontally"]])))

                      (BlueprintOutput r :mirror)))))

; Upgrade

(rum/defc ContentUpgrade <
  rum/reactive
  [r]
  (rum/adapt-class (.-Content Layout)
   {:className "content"}
   [:h2 "Upgrade (or downgrade) a blueprint"]
   [:p "Decide what common upgradeable entities (e.g. inserters) to upgrade.  Also supports downgrading (e.g. you have a great blueprint but not the tech yet)"]
   (rum/adapt-class Form
                    {:layout :vertical}
    (BlueprintInput r :upgrade))
   (when-let [blueprint (rum/react (citrus/subscription r [:upgrade :input :blueprint]))]
     (let [upgradable (upgrade/upgradeable-from-blueprint blueprint)
           order (filter upgradable upgrade/upgrades-order)]
       (rum/adapt-class Form
                        {:layout :vertical}
        (for [from order]
          (rum/adapt-class (.-Item Form) {:label (upgrade/upgrades-names from)}
                         (rum/adapt-class (.-Group Radio) {:value (rum/react (citrus/subscription r [:upgrade :config from]))
                                           :onChange #(citrus/dispatch! r :upgrade :set-config from (-> % .-target .-value))}
                                          (radio-options (mapv vector (upgrade/upgrades-by-key from))))))
        (BlueprintOutput r :upgrade))))))

; Landfill

(rum/defc ContentLandfill <
  rum/reactive
  [r]
  (rum/adapt-class (.-Content Layout)
   {:className "content"}
   [:h2 "Add landfill as tiles under a blueprint"]
   [:p "Put landfill under a blueprint"]
   (rum/adapt-class Alert {:message "Please note, that the modified blueprint can not be placed in one go in Factorio right now.  If there are entities on water, they can not be placed.  Force-place (shift) the blueprint to build the landfill and all placeable entities first, and once the landfill is in, place the blueprint again."
               :showIcon true
               :type "warning"})
   (rum/adapt-class Form
                    {:layout :vertical}
    (BlueprintInput r :landfill))
   (when (rum/react (citrus/subscription r [:landfill :input :blueprint]))
     (rum/adapt-class Form
                      {:layout :vertical}
      (rum/adapt-class (.-Item Form) {:label "Filling mode"}
                     (rum/adapt-class (.-Group Radio) {:className "input-landfill-fill-mode"
                                       :value (rum/react (citrus/subscription r [:landfill :config :fill-mode] name))
                                       :onChange #(citrus/dispatch! r :landfill :set-config :fill-mode (-> % .-target .-value keyword))}
                                      (radio-options [[:full "Full" "(complete area/bounding box of blueprint)"]
                                                      [:sparse "Sparse" "(only under entities; keeps gap for pumps)"]])))
      (rum/adapt-class (.-Item Form) {:label "Existing tiles"}
                     (rum/adapt-class (.-Group Radio) {:className "input-landfill-tile-mode"
                                       :value (rum/react (citrus/subscription r [:landfill :config :tile-mode] name))
                                       :onChange #(citrus/dispatch! r :landfill :set-config :tile-mode (-> % .-target .-value keyword))}
                                      (radio-options [[:remove "Remove" "(all tiles are removed)"]
                                                      [:replace "Replace" "(tiles are removed, but landfill is also added where tiles where honouring the filling mode)"]
                                                      [:to-book "Blueprint book" "(separate blueprint for landfill and original as book)"]])))
      (rum/adapt-class (.-Item Form) {:label "Deny landfill for entities with name"}
                       (rum/adapt-class Transfer {:className "input-landfill-entity-deny-mode"
                                                  :showSearch true
                                                  :render #(.-key %)
                                                  :dataSource (apply array (map #(js-obj "key" %) landfill-controller/entity-deny-options))
                                                  :targetKeys (rum/react (citrus/subscription r [:landfill :config :entity-deny] (partial apply array)))
                                                  :onChange #(citrus/dispatch! r :landfill :set-config :entity-deny (set %))}))
      (BlueprintOutput r :landfill)))))

; Split

(rum/defc ContentSplit <
  rum/reactive
  [r]
  (rum/adapt-class (.-Content Layout)
   {:className "content"}
   [:h2 "Splits a blueprint into multiple tiles"]
   [:p "Split a large blueprint into tiles to make it easier to place in game"]
   (rum/adapt-class Form
                    {:layout :vertical}
    (BlueprintInput r :split))
   (when (rum/react (citrus/subscription r [:split :input :blueprint]))
     [:div
      (rum/adapt-class Form
                       {:layout :vertical}
       (rum/adapt-class (.-Item Form) {:label "Size of one tile"}
                      (rum/adapt-class InputNumber {:className "input-split-tile-size"
                                         :value (rum/react (citrus/subscription r [:split :config :tile-size]))
                                         :onChange #(citrus/dispatch! r :split :set-config :tile-size %)
                                         :min 32}))
       (BlueprintOutput r :split))])))

; Buffer

(rum/defc ContentBuffer <
  rum/reactive
  [r]
  (rum/adapt-class (.-Content Layout)
   {:className "content"}
   [:h2 "Create buffer chests"]
   [:p "Turn a blueprint into a blueprint for buffer chests requesting the initial blueprint"]
   (rum/adapt-class Alert {:message "This is currently under development"
               :showIcon true
               :type "warning"})
   (rum/adapt-class Form
                    {:layout :vertical}
    (BlueprintInput r :buffer))
   (when (rum/react (citrus/subscription r [:buffer :input :blueprint]))
     [:div
      (rum/adapt-class Form
                       {:layout :vertical}
       (BlueprintOutput r :buffer))])))

; Debug

(defn pprint
  [edn]
  (with-out-str
      (cljs.pprint/pprint edn)))

(rum/defc ContentDebug <
  rum/reactive
  [r]
  (rum/adapt-class (.-Content Layout)
   {:className "content"}
   [:h2 "Show the content of a blueprint"]
   (rum/adapt-class Form
                    {:layout :vertical}
    (BlueprintInput r :debug)
    (when (rum/react (citrus/subscription r [:debug :input :blueprint]))
      (rum/adapt-class (.-Item Form) {:label "EDN"}
                       (rum/adapt-class (.-TextArea Input)
                                        {:style {:fontFamily "monospace"}
                                         :autoSize true
                                         :value (pprint (rum/react (citrus/subscription r [:debug :output])))}))))))

;;; Main

; Navigation

(defonce navigations
  [{:key "about"    :icon (.-InfoCircleOutlined  Icons) :title "About"        :component ContentAbout}
   {:key "tile"     :icon (.-BorderInnerOutlined Icons) :title "Tile"         :component ContentTile}
   {:key "split"    :icon (.-ScissorOutlined     Icons) :title "Split"        :component ContentSplit}
   {:key "mirror"   :icon (.-SwapOutlined        Icons) :title "Mirror"       :component ContentMirror}
   {:key "upgrade"  :icon (.-ToolOutlined        Icons) :title "Upgrade"      :component ContentUpgrade}
   {:key "landfill" :icon (.-TableOutlined       Icons) :title "Landfill"     :component ContentLandfill}
   {:key "buffer"   :icon (.-FilterOutlined      Icons) :title "Buffer-Chest" :component ContentBuffer}
   {:key "debug"    :icon (.-BugOutlined         Icons) :title "Debug"        :component ContentDebug}
   {:key "settings" :icon (.-SettingOutlined     Icons) :title "Settings"     :component ContentSettings}]
  )

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
  #js {:key (key-to-route key)
       :icon  (rum/adapt-class icon {})
       :label title})

(rum/defc AppHeader < rum/static []
  (rum/adapt-class (.-Header Layout)
   {:style {:paddingLeft "16px"}}
   [:h1
    (rum/adapt-class (.-SettingFilled Icons) {:style {:paddingRight "12px"}})
    "Factorio Blueprint Tools"]))

(rum/defc AppFooter < rum/static []
  (rum/adapt-class (.-Footer Layout)
   {:style {:textAlign "center"}}
   [:span
    "Copyright © 2024 Christoph Frick"
    " — "
    [:a {:href "https://github.com/christoph-frick/factorio-blueprint-tools"} "Source code"]
    " — "
    [:a {:href "https://github.com/christoph-frick/factorio-blueprint-tools/issues"} "Found an issue?"]]))

(rum/defc App < rum/reactive
  [r]
  (let [color "rgb(255, 230, 192)"]
    (rum/adapt-class ConfigProvider
                     {:theme {:algorithm darkAlgorithm
                              :token {:colorPrimary color
                                      :colorLink "orange"
                                      :colorPrimaryBg "rgb(49, 48, 49)"}

                              :components
                              {:Layout {"headerColor" "orange"
                                        :headerBg "rgb(49, 48, 49)" }} }}
   (rum/adapt-class Layout {:className "root-layout"}
              (AppHeader)
              (let [{:keys [current navigations navigations-by-key]} (rum/react (citrus/subscription r [:navigation]))]
                (rum/adapt-class Layout
                 (rum/adapt-class (.-Sider Layout)
                  {:theme "light"}
                  (rum/adapt-class Menu
                                   {:theme "light"
                                    :mode "inline"
                                    :selectedKeys [current]
                                    :onSelect #(nav! (.-key %))
                                    :items (apply array (map menu-item navigations))}))
                 (rum/adapt-class Layout
                    ((:component (navigations-by-key current)) r)
                    (AppFooter))))))))

(defonce init-ctrl
  (citrus/broadcast-sync! reconciler :init))

(defn init!
  []
  (rum/mount (App reconciler) (js/document.getElementById "app")))

(defn main
  []
  (init!)
  (pushy/start! history))

(main)
