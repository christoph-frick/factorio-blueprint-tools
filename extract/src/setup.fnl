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
                      "/var/factorio-data/core/lualib/?.lua" ";"))
(require :dataloader)

; FIXME: the package.path/require works for now.  `dofile` might be the proper way
(local package-path package.path)

(set package.path (.. package-path ";"
                      "/var/factorio-data/core/?.lua" ";"
                      "/var/factorio-data/?.lua"))
(require :core.data)

(set package.path (.. package-path ";"
                      "/var/factorio-data/base/?.lua" ";"
                      "/var/factorio-data/?.lua" ";"
                      "/var/factorio-blueprint-tools/?.lua"))
(require :base.data)

(local view (require :fennelview))

(fn pr [m]
    (print (view m)))

(fn extract
    [key]
    (let [result {}]
        (each [ok ov (pairs _G.data.raw)]
            (each [ik iv (pairs ov)]
                (when (. iv key)
                    (tset result ik (. iv key)))))
        result))

{: pr
 : extract}
