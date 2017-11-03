(ns factorio-blueprint-tools.core
  (:require [factorio-blueprint-tools.tile :refer [tile]]
            [factorio-blueprint-tools.serialization :as ser]
            [antizer.rum :as ant]
            [rum.core :as rum]))

(enable-console-print!)

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
               :type "warning"})
   (ant/form
    (ant/form-item {:label "Factorio entities"}
                   (ant/select {:value "vanilla-0.15"}
                               (ant/select-option {:key "vanilla-0.15"} "Vanilla 0.15"))))))

(defonce blueprint-tile-state
  (atom ""))

(defonce tile-settings-state
  (atom
   {::blueprint-error nil ; maybe an error
    ::blueprint nil ; the blueprint, unless there is an error
    ::tile-x 2 ; initial values for the tiling
    ::tile-y 2}))

(defonce update-blueprint-tile-watch
  (add-watch blueprint-tile-state ::update-blueprint-tile
             (fn [_ _ _ blueprint-string]
               (letfn [(update-fn
                         [blueprint error]
                         (swap! tile-settings-state assoc
                                ::blueprint blueprint
                                ::blueprint-error error))]
                 (if (seq blueprint-string)
                   (try
                     (update-fn (ser/decode blueprint-string) nil)
                     (catch :default e
                       (update-fn nil (str "Could not load blueprint.  Please make sure to copy and paste the whole string from Factorio. (Error: " e ")"))))
                   (update-fn nil nil))))))

(defonce tile-result-state
  (rum/derived-atom [tile-settings-state] ::tile-result
                    (fn [{::keys [blueprint tile-x tile-y] :as tile-settings}]
                      (some-> blueprint (tile tile-x tile-y) (ser/encode)))))

(rum/defcs content-tile <
  rum/reactive
  []
  (let [blueprint (rum/cursor tile-settings-state ::blueprint)
        blueprint-error (rum/cursor tile-settings-state ::blueprint-error)
        tile-x (rum/cursor tile-settings-state ::tile-x)
        tile-y (rum/cursor tile-settings-state ::tile-y)]
    (ant/layout-content
     {:style {:padding "1ex 1em"}}
     [:h1 "Tile a blueprint"]
     (ant/form
      (ant/form-item {:label "Blueprint string"
                      :help "Copy a blueprint string from Factorio and paste it in this field"}
                     (ant/input-text-area (assoc ta-no-spellcheck
                                                 :value (rum/react blueprint-tile-state)
                                                 :onChange #(reset! blueprint-tile-state (-> % .-target .-value))
                                                 :onFocus #(.select (-> % .-target)))))
      (when-let [error-message (rum/react blueprint-error)]
        (ant/alert {:message error-message
                    :showIcon true
                    :type "error"}))
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
         (ant/form-item {:label "Result"
                         :help "Copy this blueprint string and import in from the blueprint library in Factorio"}
                        (ant/input-text-area (assoc ta-no-spellcheck
                                                    :value (rum/react tile-result-state)
                                                    :onFocus #(.select (-> % .-target)))))))))))

(def navigations
  [{:key "about" :icon "info-circle-o" :title "About" :component content-about}
   {:key "tile" :icon "appstore-o" :title "Tile" :component content-tile}
   {:key "settings " :icon "setting" :title "Settings (Vanilla 0.15)" :component content-settings}])

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
                 "Copyright © 2017 Christoph Frick"
                 " "
                 [:a {:href "https://github.com/christoph-frick/factorio-blueprint-tools"} "https://github.com/christoph-frick/factorio-blueprint-tools"]]))))

(defn init!
  []
  (rum/mount (render) (js/document.getElementById "app")))

(init!)

(defn on-js-reload [])
