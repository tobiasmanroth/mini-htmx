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

(defn random-yyyyMMdd
  ([format]
   (let [youtube-founded (java.time.LocalDate/of 2005 2 14)
         today (java.time.LocalDate/now)
         days-between (.between java.time.temporal.ChronoUnit/DAYS youtube-founded today)
         random-days (rand-int (inc days-between))
         random-date (.plusDays youtube-founded random-days)
         formatter (java.time.format.DateTimeFormatter/ofPattern format)]
     (.format random-date formatter)))
  ([]
   (random-yyyyMMdd "yyyyMMdd")))

(comment
  (random-ymd)
  )

(defn random-yyyy
  []
  (let [youtube-founded (java.time.LocalDate/of 2005 2 14)
        today (java.time.LocalDate/now)
        years-between (.between java.time.temporal.ChronoUnit/YEARS youtube-founded today)
        random-years (rand-int (inc years-between))
        random-date (.plusYears youtube-founded random-years)
        formatter (java.time.format.DateTimeFormatter/ofPattern "yyyy")]
    (.format random-date formatter)))

(comment
  (random-yyyy)
  )



(def registry
  {:padded-digits #'random-padded-int
   :random-yyyyMMdd #'random-yyyyMMdd
   :random-integer #'rand-int
   :random-yyyy #'random-yyyy})

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

(def new-videos
  [["IMG"]
   ["MVI"]
   [[:random-yyyyMMdd]]
   ["WIN " [:random-yyyyMMdd]]
   ["Capture " [:random-yyyyMMdd]]
   ["VID " [:random-yyyyMMdd]]
   ["\"My Movie " [:random-integer 100] "\""]
   ["\"My Edited Video\""]
   ["/Storage/Emulated/"]
   ["PXL"]
   ["InShot " [:random-yyyyMMdd]]
   ["WhatsApp Video " [:random-yyyy]]
   ["FullSizeRender"]
   ["RpReplay"]
   ["VTS 01"]
   ["DVR"]
   ["VLC Record"]
   ["Robloxapp"]
   ["\".MP4\" | \".3gp\" | \".MOV\" | \".AVI\" | \".WMV\""]
   ["\".FLAC\""]
   ["\".WAV\""]
   ["Recording gvo"]
   ["Lv 0"]
   ["bmdjAAAF"]
   ["YouCut " [:random-yyyyMMdd]]
   ["\"Video " [:random-yyyyMMdd] "\""]
   ["\"Copy of Copy of\""]
   ["\"Untitled video\""]
   ["\"YTPMV\""]
   ["\"Klasky Csupo\""]
   ["\"Com Oculus Vrshell\""]
   ["\"Com Oculus Metacam\""]
   ["Desktop " [:random-yyyyMMdd "yyyy MM dd"]]
   ])

(defn generate-string
  [fmt]
  (apply str
         (map
           (fn [f]
             (cond
               (string? f)
               f

               (registry (first f))
               (if-let [args (seq (rest f))]
                 (apply (registry (first f))
                        args)
                 ((registry (first f))))
               ))
           fmt)))

(defn random-search-query
  []
  (generate-string (rand-nth forgotten-videos))
  )

(comment
  (random-search-query)
  )
