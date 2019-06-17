(ns factorio-blueprint-tools.core
  (:require [factorio-blueprint-tools.tile :as tile]
            [factorio-blueprint-tools.mirror :as mirror]
            [factorio-blueprint-tools.upgrade :as upgrade]
            [factorio-blueprint-tools.landfill :as landfill]
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
                    (preview/preview blueprint))]))

; About

(rum/defc ContentAbout < rum/static
  []
  (ant/layout-content
   {:style {:padding "1ex 1em"}}
   [:h2 "Random tools to manipulate Factorio blueprint strings"]
   [:p "While there are already some of those functions built as mods to the game, one can not use mods while playing for the achievements"]
   [:h3 "Instructions"]
   [:p "Pick a tool on the left hand side in the menu:"]
   [:ul
    [:li [:em "Tile"] ": Arrange copies of the blueprint in a grid.  E.g. take a six electric miner blueprint and tile 15x15 to cover even the biggest resource fields"]
    [:li [:em "Mirror"] ": Mirror the blueprint either vertically or horizontally"]
    [:li [:em "Upgrade"] ": Decide what common upgradeable entities (e.g. inserters) to upgrade.  Also supports downgrading (e.g. you have a great blueprint but not the tech yet)"] 
    [:li [:em "Landfill"] ": Put landfill under a blueprint"]]
   [:p "Then paste the blueprint string either from the game or from a different place into the input field, adjust the settings, and finally copy the final blueprint and import it into Factorio"]))

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
                   (ant/select {:value "vanilla-0.17"}
                               (ant/select-option {:key "vanilla-0.17"} "Vanilla 0.17"))))))

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
   (ant/form
    (BlueprintInput r :landfill))
   (when (rum/react (citrus/subscription r [:landfill :input :blueprint]))
     [:div
      (ant/form
       (BlueprintOutput r :landfill))])))

;;; Main

(def navigations
  [{:key "about" :icon "info-circle-o" :title "About" :component ContentAbout}
   {:key "tile" :icon "appstore-o" :title "Tile" :component ContentTile}
   {:key "mirror" :icon "swap" :title "Mirror" :component ContentMirror}
   {:key "upgrade" :icon "tool" :title "Upgrade" :component ContentUpgrade}
   {:key "landfill" :icon "table" :title "Landfill" :component ContentLandfill}
   {:key "settings " :icon "setting" :title "Settings" :component ContentSettings}])

(def navigations-by-key
  (into {} (map (juxt :key identity)) navigations))

;; Controller (might end up in a differnt file

; Navigation

(defmulti navigation identity)

(defmethod navigation :init []
  {:state (-> navigations first :key)})

(defmethod navigation :goto [_ [target] _]
  {:state target})

; Helpers

(def default-tool-state
  {:input {:encoded nil
           :blueprint nil
           :error nil}
   :config {}
   :output {:encoded nil
            :blueprint nil}})

(defn decode-blueprint
  [encoded-blueprint]
  (if (or (not encoded-blueprint) (str/blank? encoded-blueprint))
    [nil nil]
    (try
      [(ser/decode encoded-blueprint) nil]
      (catch :default e
        [nil e]))))

(defn- set-blueprint
  [state encoded-blueprint]
  (let [[blueprint error] (decode-blueprint encoded-blueprint)]
    (update state :input assoc :encoded encoded-blueprint :blueprint blueprint :error error)))

(defn- set-config
  [state k v]
  (update state :config assoc k v))

(defn- update-result
  [state default-config update-fn]
  (let [blueprint (some-> state :input :blueprint)
        config (or (some-> state :config) default-config)
        result (some-> blueprint (update-fn config))
        encoded-result (some-> result ser/encode)]
    (update state :output assoc :blueprint result :encoded encoded-result)))


; Tile

(def default-tile-config
  {:tile-x 2
   :tile-y 2})

(defmulti tile identity)

(defmethod tile :init []
  {:state (assoc default-tool-state
                 :config default-tile-config)})

(defmethod tile :set-blueprint [r [encoded-blueprint] state]
  {:state (set-blueprint state encoded-blueprint)
   :dispatch [[:tile :update]]})

(defmethod tile :set-config [r [k v] state]
  {:state (set-config state k v)
   :dispatch [[:tile :update]]})

(defmethod tile :update [_ _ state]
  {:state (update-result state
                         default-tile-config
                         (fn tile [blueprint {:keys [tile-x tile-y] :as config}]
                           (tile/tile blueprint tile-x tile-y)))})

; Mirror

(def default-mirror-config
  {:direction :vertically})

(defmulti mirror identity)

(defmethod mirror :init []
  {:state (assoc default-tool-state
                 :config default-mirror-config)})

(defmethod mirror :set-blueprint [_ [encoded-blueprint] state]
  {:state (set-blueprint state encoded-blueprint)
   :dispatch [[:mirror :update]]})

(defmethod mirror :set-config [_ [k v] state]
  {:state (set-config state k v)
   :dispatch [[:mirror :update]]})

(defmethod mirror :update [_ _ state]
  {:state (update-result state
                         default-mirror-config
                         (fn mirror [blueprint {:keys [direction] :as config}]
                           (mirror/mirror blueprint direction)))})


; Upgrade

(defmulti upgrade identity)

(defmethod upgrade :init []
  {:state (assoc default-tool-state
                 :config upgrade/default-upgrade-config)})

(defmethod upgrade :set-blueprint [_ [encoded-blueprint] state]
  {:state (set-blueprint state encoded-blueprint)
   :dispatch [[:upgrade :update]]})

(defmethod upgrade :set-config [_ [k v] state]
  {:state (set-config state k v)
   :dispatch [[:upgrade :update]]})

(defmethod upgrade :update [_ _ state]
  {:state (update-result state
                         upgrade/default-upgrade-config
                         (fn upgrade [blueprint config]
                           (upgrade/upgrade-blueprint config blueprint)))})

; Landfill

(def default-landfill-config
  {})

(defmulti landfill identity)

(defmethod landfill :init []
  {:state (assoc default-tool-state
                 :config default-landfill-config)})

(defmethod landfill :set-blueprint [_ [encoded-blueprint] state]
  {:state (set-blueprint state encoded-blueprint)
   :dispatch [[:landfill :update]]})

(defmethod landfill :set-config [_ [k v] state]
  {:state (set-config state k v)
   :dispatch [[:landfill :update]]})

(defmethod landfill :update [_ _ state]
  {:state (update-result state
                         default-landfill-config
                         (fn landfill [blueprint _]
                           (landfill/landfill blueprint)))})

;; Effect Handlers

(defn dispatch [r _ events]
  (doseq [[ctrl & args] events]
    (apply citrus/dispatch! (into [r ctrl] args))))


;; Reconciler

(defonce reconciler
  (citrus/reconciler
   {:state (atom {})
    :controllers {:navigation navigation
                  :tile tile
                  :mirror mirror
                  :upgrade upgrade 
                  :landfill landfill}
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
    "Copyright © 2019 Christoph Frick"
    " "
    [:a {:href "https://github.com/christoph-frick/factorio-blueprint-tools"} "https://github.com/christoph-frick/factorio-blueprint-tools"]]))

(rum/defc App < rum/reactive
  [r]
  (ant/layout {:style {:min-height "100vh"}}
              (AppHeader)
              (ant/layout (ant/layout-sider
                           {:theme "light"}
                           (ant/menu {:theme "light"
                                      :mode "inline"
                                      :selectedKeys [(rum/react (citrus/subscription r [:navigation]))]
                                      :onSelect #(citrus/dispatch! r :navigation :goto (.-key %))
                                      :style {:min-height "calc(100vh-64px)"}}
                                     (map menu-item navigations)))
                          (ant/layout
                           (let [nav-key (rum/react (citrus/subscription r [:navigation]))]
                             (if-let [nav-item (navigations-by-key nav-key)]
                               ((:component nav-item) r)
                               (do
                                 (ContentAbout r)
                                 (ant/message-error (str "Unknown navigation target: " nav-key)))))
                           (AppFooter)))))

(defonce init-ctrl
  (citrus/broadcast-sync! reconciler :init))

(defn init!
  []
  (rum/mount (App reconciler) (js/document.getElementById "app")))

(init!)

(defn on-js-reload [])
