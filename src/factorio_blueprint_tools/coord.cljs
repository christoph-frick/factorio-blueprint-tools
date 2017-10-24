(ns factorio-blueprint-tools.coord)

(def directions {:north 0
                 :northeast 1
                 :east 2
                 :southeast 3
                 :south 4
                 :southwest 5
                 :west 6
                 :northwest 7})

(def rotation-matrices {(directions :north) [[1 0] [0 1]]
                        (directions :east) [[0 1] [-1 0]]
                        (directions :south) [[-1 0] [0 -1]]
                        (directions :west) [[0 -1] [1 0]]})

(defn coord 
  [x y]
  [x y])

(defn box 
  [a b]
  [a b])

(defn rotate-coord 
  [[x y] dir]
  (let [[[a b] [c d]] (rotation-matrices (or dir 0))]
    (coord
     (+ (* x a) (* y b))
     (+ (* x c) (* y d)))))

(defn translate-coord 
  [[x y] [v t]]
  (coord
   (+ x v)
   (+ y t)))

(defn rotate-box 
  [[a b] dir]
  (box
   (rotate-coord a dir)
   (rotate-coord b dir)))

(defn translate-box 
  [[a b] offset]
  (box
   (translate-coord a offset)
   (translate-coord b offset)))

(defn union-box
  [[[ax1 ay1] [ax2 ay2]] [[bx1 by1] [bx2 by2]]]
  (box
   (coord
    (min ax1 ax2 bx1 bx2)
    (min ay1 ay2 by1 by2))
   (coord
    (max ax1 ax2 bx1 bx2)
    (max ay1 ay2 by1 by2))))
