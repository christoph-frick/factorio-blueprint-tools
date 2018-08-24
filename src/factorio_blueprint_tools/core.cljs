(ns factorio-blueprint-tools.core
  (:require [factorio-blueprint-tools.tile :as tile]
            [factorio-blueprint-tools.mirror :as mirror]
            [factorio-blueprint-tools.upgrade :as upgrade]
            [factorio-blueprint-tools.serialization :as ser]
            [antizer.rum :as ant]
            [rum.core :as rum]))

(enable-console-print!)

(def ta-no-spellcheck
  {:autoComplete "off"
   :autoCorrect "off"
   :autoCapitalize "off"
   :spellCheck "false"})

(def blueprint-state
  {::blueprint-error nil ; maybe an error
   ::blueprint nil ; the blueprint, unless there is an error
   })

(rum/defc content-about < rum/static
  []
  (ant/layout-content
   {:style {:padding "1ex 1em"}}
   [:h1 "Factorio Blueprint Tools"]
   [:h2 "Random tools to manipulate Factorio blueprint strings"]
   [:p "While there are already some of those functions built as mods to the game, one can not use mods while playing for the achievements"]))

(rum/defc content-settings < rum/static
  []
  (ant/layout-content
   {:style {:padding "1ex 1em"}}
   [:h1 "Settings"]
   (ant/alert {:message "Currently there is no way to change or add mods etc. for the sizes occupied by the entities."
               :showIcon true
               :type "warning"})
   (ant/form
    (ant/form-item {:label "Factorio entities"}
                   (ant/select {:value "vanilla-0.16"}
                               (ant/select-option {:key "vanilla-0.16"} "Vanilla 0.16"))))))

(defn build-blueprint-watch
  [watch-name blueprint-string-atom blueprint-target-atom]
  (add-watch blueprint-string-atom watch-name
             (fn [_ _ _ blueprint-string]
               (letfn [(update-fn
                         [blueprint error]
                         (swap! blueprint-target-atom assoc
                                ::blueprint blueprint
                                ::blueprint-error error))]
                 (if (seq blueprint-string)
                   (try
                     (update-fn (ser/decode blueprint-string) nil)
                     (catch :default e
                       (update-fn nil (str "Could not load blueprint.  Please make sure to copy and paste the whole string from Factorio. (Error: " e ")"))))
                   (update-fn nil nil))))))

(defn blueprint-state-cursors
  "Create rum/cursors for ::blueprint, ::blueprint-error, and each optional key passed and returns as vector in that order"
  [state & ks]
  (let [ks (concat [::blueprint ::blueprint-error] ks)]
    (mapv #(rum/cursor state %) ks)))

(defn form-item-input-blueprint
  [state]
  (ant/form-item {:label "Blueprint string"
                  :help "Copy a blueprint string from Factorio and paste it in this field"}
                 (ant/input-text-area (assoc ta-no-spellcheck
                                             :value (rum/react state)
                                             :onChange #(reset! state (-> % .-target .-value))
                                             :onFocus #(.select (-> % .-target))))))

(defn form-item-output-blueprint
  [state]
  (ant/form-item {:label "Result"
                  :help "Copy this blueprint string and import in from the blueprint library in Factorio"}
                 (ant/input-text-area (assoc ta-no-spellcheck
                                             :value (rum/react state)
                                             :onFocus #(.select (-> % .-target))))))

(defn alert-error
  [error-message]
  (ant/alert {:message error-message
              :showIcon true
              :type "error"}))

(defonce blueprint-tile-state
  (atom ""))

(defonce tile-settings-state
  (atom
   (assoc blueprint-state
          ::tile-x 2 ; initial values for the tiling
          ::tile-y 2)))

(defonce update-blueprint-tile-watch
  (build-blueprint-watch ::update-blueprint-tile blueprint-tile-state tile-settings-state))

(defonce tile-result-state
  (rum/derived-atom [tile-settings-state] ::tile-result
                    (fn [{::keys [blueprint tile-x tile-y] :as tile-settings}]
                      (some-> blueprint (tile/tile tile-x tile-y) (ser/encode)))))

(rum/defcs content-tile <
  rum/reactive
  []
  (let [[blueprint blueprint-error tile-x tile-y] (blueprint-state-cursors tile-settings-state ::tile-x ::tile-y)]
    (ant/layout-content
     {:style {:padding "1ex 1em"}}
     [:h1 "Tile a blueprint"]
     (ant/form
      (form-item-input-blueprint blueprint-tile-state)
      (when-let [error-message (rum/react blueprint-error)]
        (alert-error error-message))
      (when (rum/react blueprint)
        (ant/form
         (ant/form-item {:label "Tiles on X axis"}
                        (ant/input-number {:value (rum/react tile-x)
                                           :onChange #(reset! tile-x %)
                                           :min 2}))
         (ant/form-item {:label "Tiles on Y axis"}
                        (ant/input-number {:value (rum/react tile-y)
                                           :onChange #(reset! tile-y %)
                                           :min 2}))
         (form-item-output-blueprint tile-result-state)))))))

;; TODO: dedupe this more with tile and others to come
(defonce blueprint-mirror-state
  (atom ""))

(defonce mirror-settings-state
  (atom blueprint-state))

(defonce update-blueprint-mirror-watch
  (build-blueprint-watch ::update-blueprint-mirror blueprint-mirror-state mirror-settings-state))

(defonce mirror-result-state
  (rum/derived-atom [mirror-settings-state] ::mirror-result
                    (fn [{::keys [blueprint] :as mirror-settings}]
                      (some-> blueprint (mirror/mirror) (ser/encode)))))

(rum/defcs content-mirror <
  rum/reactive
  []
  (let [[blueprint blueprint-error] (blueprint-state-cursors mirror-settings-state)]
    (ant/layout-content
     {:style {:padding "1ex 1em"}}
     [:h1 "Mirror a blueprint"]
     (ant/form
      (form-item-input-blueprint blueprint-mirror-state)
      (when-let [error-message (rum/react blueprint-error)]
        (alert-error error-message))
      (when (rum/react blueprint)
        (ant/form
         (form-item-output-blueprint mirror-result-state)))))))

;; TODO: dedupe this more with tile and others to come
(defonce blueprint-upgrade-state
  (atom ""))

(defonce upgrade-settings-state
  (atom
   (assoc blueprint-state
          ::upgrade-config upgrade/default-upgrade-config)))

(defonce update-blueprint-upgrade-watch
  (build-blueprint-watch ::update-blueprint-upgrade blueprint-upgrade-state upgrade-settings-state))

(defonce upgrade-result-state
  (rum/derived-atom [upgrade-settings-state] ::upgrade-result
                    (fn [{::keys [blueprint upgrade-config] :as upgrade-settings}]
                      (some->> blueprint (upgrade/upgrade-blueprint upgrade-config) (ser/encode)))))

(rum/defcs content-upgrade <
  rum/reactive
  []
  (let [[blueprint blueprint-error upgrade-config] (blueprint-state-cursors upgrade-settings-state ::upgrade-config)]
    (ant/layout-content
     {:style {:padding "1ex 1em"}}
     [:h1 "Upgrade (or downgrade) a blueprint"]
     (ant/form
      (form-item-input-blueprint blueprint-upgrade-state)
      (when-let [error-message (rum/react blueprint-error)]
        (alert-error error-message))
      (when-let [blueprint (rum/react blueprint)]
        (let [upgradable (upgrade/upgradeable-from-blueprint blueprint)
              order (filter upgradable upgrade/upgrades-order)
              cfg (rum/react upgrade-config)]
          (ant/form
           (for [from order]
             (ant/form-item {:label (upgrade/upgrades-names from)}
                            (ant/radio-group {:value (cfg from)
                                              :onChange #(swap! upgrade-config assoc from (-> % .-target .-value))}
                                             (for [option (upgrade/upgrades-by-key from)]
                                               (ant/radio {:key option :value option} (upgrade/upgrades-names option))))))
           (form-item-output-blueprint upgrade-result-state))))))))

(def navigations
  [{:key "about" :icon "info-circle-o" :title "About" :component content-about}
   {:key "tile" :icon "appstore-o" :title "Tile" :component content-tile}
   {:key "mirror" :icon "swap" :title "Mirror" :component content-mirror}
   {:key "upgrade" :icon "retweet" :title "Upgrade" :component content-upgrade}
   {:key "settings " :icon "setting" :title "Settings" :component content-settings}])

(def navigations-by-key
  (into {} (map (juxt :key identity)) navigations))

(defonce navigation-state
  (atom (-> navigations first :key)))

(defn- menu-item
  [{:keys [key icon title]}]
  (ant/menu-item {:key key} [:span (ant/icon {:type icon}) title]))

(rum/defc render < rum/reactive
  []
  (ant/layout {:style {:min-height "100vh"}}
              (ant/layout-sider
               (ant/menu {:theme "dark"
                          :mode "inline"
                          :selectedKeys [(rum/react navigation-state)]
                          :onSelect #(reset! navigation-state (.-key %))
                          :style {:line-height "64px"}}
                         (map menu-item navigations)))
              (ant/layout
               (let [nav-key (rum/react navigation-state)]
                 (if-let [nav-item (navigations-by-key nav-key)]
                   ((:component nav-item))
                   (do
                     (content-about)
                     (ant/message-error (str "Unknown navigation target: " nav-key)))))
               (ant/layout-footer
                [:span
                 "Copyright © 2018 Christoph Frick"
                 " "
                 [:a {:href "https://github.com/christoph-frick/factorio-blueprint-tools"} "https://github.com/christoph-frick/factorio-blueprint-tools"]]))))

(defn init!
  []
  (rum/mount (render) (js/document.getElementById "app")))

(init!)

(defn on-js-reload [])
