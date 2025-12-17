(ns mini-htmx.youtube-recycle-bin)

(defn digit-count
  [n]
  (count (str (Math/abs n))))

(defn random-padded-int
  "exclusive"
  ([min max]
   (format (str "%0" (digit-count max) "d")
           (+ min (rand-int (- (inc max) min)))))
  ([max]
   (random-padded-int 0 max)))

(def registry
  {:padded-digits #'random-padded-int})

;; TODO some search terms need the query parameter "&sp=CAISAhAB" to sort by upload date. How to do?
(def forgotten-videos
  [["IMG " [:padded-digits 999]]
   ["MVI " [:padded-digits 999]]
   ["MOV " [:padded-digits 999]]
   ["100 " [:padded-digits 999]]
   ["SAM " [:padded-digits 999]]
   ["DSC " [:padded-digits 999]]
   ["SDV " [:padded-digits 999]]
   ["DSCF" [:padded-digits 999]]
   ["DSCN" [:padded-digits 999]]
   ["PICT" [:padded-digits 999]]
   ["MAQ0" [:padded-digits 999]]
   ["FILE" [:padded-digits 999]]
   ["GOPR" [:padded-digits 999]]
   ["GP01" [:padded-digits 999]]
   ["GX01" [:padded-digits 999]]
   ["DJI " [:padded-digits 2000]]
   ["HNI 0" [:padded-digits 100]]
   ["WA0" [:padded-digits 999]]
   ["MOL0" [:padded-digits 100]]])

(defn generate-string
  [fmt]
  (apply str
         (map
           (fn [f]
             (cond
               (string? f)
               f

               (registry (first f))
               (apply (registry (first f))
                      (rest f))))
           fmt)))

(defn random-search-query
  []
  (generate-string (rand-nth forgotten-videos))
  )

(comment
  (let [fmt (rand-nth forgotten-videos)]
    (generate-string fmt))
  )

