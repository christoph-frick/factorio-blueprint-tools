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
  ([x y]
   [x y])
  ([s]
   (coord s s)))

(defn transform-coord
  [f [x y]]
  (coord (f x) (f y)))

(def negate-coord 
  (partial transform-coord (partial * -1)))

(def -ONE (coord -1))

(def ZERO (coord 0))

(def ONE (coord 1))

(def X (coord 1 0))

(def Y (coord 0 1))

(defn box
  [[ax ay] [bx by]]
  [(coord (min ax bx) (min ay by))
   (coord (max ax bx) (max ay by))])

(def NIL-BOX [[nil nil] [nil nil]])

(defn box-from-size
  [[x y] width height]
  (box (coord x y) (coord (+ x width) (+ y height))))

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

(defn expand-box
  [[a b] offset]
  (box
   a
   (translate-coord b offset)))

(defn union-box
  [b1 b2]
  (if (= NIL-BOX b1)
    b2
    (let [[[ax1 ay1] [ax2 ay2]] b1
          [[bx1 by1] [bx2 by2]] b2]
      (box
       (coord
        (min ax1 ax2 bx1 bx2)
        (min ay1 ay2 by1 by2))
       (coord
        (max ax1 ax2 bx1 bx2)
        (max ay1 ay2 by1 by2))))))

(defn area
  [[[min-x min-y] [max-x max-y]]]
  (coord
   (Math/ceil (- max-x min-x))
   (Math/ceil (- max-y min-y))))

(defn in-coord?
  [[min max] i]
  (and (<= min i)
       (< i max)))

(defn in-box?
  [[[min-x min-y] [max-x max-y]] [x y]]
  (and
   (in-coord? (coord min-x max-x) x)
   (in-coord? (coord min-y max-y) y)))
