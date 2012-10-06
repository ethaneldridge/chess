(ns chess.bitboard.impl.piece-attacks
  (:use [chess.bitboard.impl.file-rank])
  (:use [chess.bitboard.impl.bitoperations]))

(defn- create-vect-bitboards [moves-coords]
  "creates a vector of  64 squares which have bitboards
   in which moves  have been flaged "
  (let [all-moves  (for [[square file rank] file-rank-squares
                         [x y] moves-coords
                         :let  [f (+ file x) r (+ rank y)]
                         :when (and (> f 0 ) (< f 9) (> r 0) (< r 9))]
                     [square f r])

        bitboard-updater (fn [result [square f r]]
                           (let [bitboard  (nth result square)
                                 bit       (bit-set 0 (coord->square f r))]
                                 (assoc result square (bit-or bitboard bit))))

        empty-board-vect (apply vector (repeat 64 0))]
    (reduce bitboard-updater empty-board-vect all-moves)))

; result (make-array Long/TYPE 64)
(def knight-attack-array
  "creates a lookup array of  64 squares which have bitboards
   in which knightattacks have been flaged "
  (long-array (create-vect-bitboards
              [[1 2] [2 1] [2 -1] [1 -2] [-1 -2] [-2 -1] [-2 1] [-1 2]] )))

 (def pawn-white-move-array
   (long-array  (create-vect-bitboards [[0 1]])))

 (def pawn-white-double-move-array
   (let [row-4-squares [24 25 26 27 28 29 30 31]]
     (long-array (concat (repeat 8 0)
                         (map #(bit-set 0 %) row-4-squares)
                         (repeat 48 0)))))

(def pawn-black-move-array
  (long-array  (create-vect-bitboards [[0 -1]])))

 (def pawn-black-double-move-array
   (let [row-5-squares [32 33 34 35 36 37 38 39]]
     (long-array (concat (repeat 48 0)
                         (map #(bit-set 0 %) row-5-squares)
                         (repeat 8 0)))))

(def pawn-white-attack-array
  (long-array (create-vect-bitboards [[1 1] [-1 1]])))

(def pawn-black-attack-array
  (long-array (create-vect-bitboards [[-1 -1] [1 -1]])))

(def king-attack-array
  (long-array (create-vect-bitboards [[1 1][-1 1][-1 -1][1 -1][0 1][0 -1][1 0][-1 0]])))

;; sliding moves
;;

(defn slide-attack-byte [occupied-bits pos]
  "occupied-bits -  flags the positions of all pieces of a rank,file or diagonal.
   pos  -  the current square  position of the current piece
   returns  a  long where the first 8-bits flag the squares to which an piece on position pos
   can move (including any attacked pieces)
   Example : (slide-attack-bits 2r10001001 5) -> 2r00001011"

  (let [bit-vect (bit->vector occupied-bits 8)
        indexed-bits (indexed-bits vector bit-vect)
        nearest-left  (nth (last  (filter (fn [[idx bit]] (< idx pos)) indexed-bits)) 0 0)
        nearest-right (nth (first (filter (fn [[idx bit]] (> idx pos)) indexed-bits)) 0 9)]
    (vector->bit (for [idx (range 8)]
                             (cond (= idx pos)           0
                                   (< idx nearest-left)  0
                                   (> idx nearest-right) 0
                                   :else                 1)))))

(defn slide-attack->bitboard[square bits]
   "shifts bits to square position in a empty bitboard"
   (bit-shift-left bits (aget rank-shift-array square)))


(defn- slide-attack-column->bitboard[square bits]
   "shifts bits to square position in a empty bitboard"
   (bit-shift-left bits (square->column square)))

(def attack-array-ranks
"2 dim array on square and occupied positions"
  (to-array-2d
   (map (fn [square]
          (for [occupied (range 64)]
            (slide-attack->bitboard square
              (slide-attack-byte (bit-shift-left occupied 1) (square->column square)))))
        (range 64))))

(def attack-array-files
"2 dim array on square and occupied positions"
  (to-array-2d
   (map (fn [square]
          (for [occupied (range 64)]
            (slide-attack-column->bitboard square
               (rotate90-bitboard-clockwise
                (slide-attack-byte (bit-shift-left occupied 1) (- 7 (square->row square)))))))
        (range 64))))


(def attack-array-diagonal-a1h8
"2 dim array on square and occupied positions"
  (to-array-2d
   (map (fn [square]
          (for [occupied (range 64)]
            (rotate-bitboard-45-anticlockwise square
             (slide-attack->bitboard square
                (slide-attack-byte (bit-shift-left occupied 1) (square->column square))))))
        (range 64))))

(def attack-array-diagonal-a8h1
"2 dim array on square and occupied positions"
  (to-array-2d
   (map (fn [square]
          (for [occupied (range 64)]
            (rotate-bitboard-45-clockwise square
             (slide-attack->bitboard square
               (slide-attack-byte  (bit-shift-left occupied 1) (square->column square))))))
        (range 64))))