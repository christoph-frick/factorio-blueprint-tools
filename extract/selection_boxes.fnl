#!/usr/bin/env fennel

; FIXME: is this really needed?
(global defines {:direction {:north 0
                             :northeast 1
                             :east 2
                             :southeast 3
                             :south 4
                             :southwest 5
                             :west 6
                             :northwest 7}
                 :difficulty_settings {:recipe_difficulty {:normal 0}
                                       :technology_difficulty {:normal 0}}})

(set package.path (.. package.path ";" 
                      "/var/factorio-data/core/lualib/?.lua" ";" 
                      "/var/factorio-data/base/?.lua"))
(require :dataloader)
(require :data)

(local view (require :fennelview))

; -- find all entities with a selection_box
(print 
  (view 
    (let [result {}]
      (each [ok ov (pairs data.raw)]
        (each [ik iv (pairs ov)]
          (when iv.selection_box
            (tset result ik iv.selection_box))))
      result)))
