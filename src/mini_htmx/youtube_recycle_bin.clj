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
  [{:keys [format start-year]
    :or {format "yyyyMMdd"}}]
  (let [youtube-founded (java.time.LocalDate/of 2005 2 14)
        start-date (if start-year
                     (java.time.LocalDate/of start-year 1 1)
                     youtube-founded)
        today (java.time.LocalDate/now)
        days-between (.between java.time.temporal.ChronoUnit/DAYS start-date today)
        random-days (rand-int (inc days-between))
        random-date (.plusDays start-date random-days)
        formatter (java.time.format.DateTimeFormatter/ofPattern format)]
    (.format random-date formatter)))

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

(defn random-character
  [s]
  (rand-nth (seq s)))

(defn random-hhmmss
  []
  (let [hours (rand-int 24)
        minutes (rand-int 60)
        seconds (rand-int 60)]
    (format "%02d%02d%02d" hours minutes seconds)))

(comment
  (random-hhmmss)
  )

(def registry
  {:padded-digits #'random-padded-int
   :random-yyyyMMdd #'random-yyyyMMdd
   :random-integer #'rand-int
   :random-yyyy #'random-yyyy
   :random-character #'random-character
   :random-hhmmss #'random-hhmmss})

(def forgotten-videos
  [["IMG " [:padded-digits 9999]]
   ["MVI " [:padded-digits 9999]]
   ["MOV " [:padded-digits 9999]]
   ["100 " [:padded-digits 9999]]
   ["SAM " [:padded-digits 9999]]
   ["DSC " [:padded-digits 9999]]
   ["SDV " [:padded-digits 9999]]
   ["DSCF" [:padded-digits 9999]]
   ["DSCN" [:padded-digits 9999]]
   ["PICT" [:padded-digits 9999]]
   ["MAQ0" [:padded-digits 9999]]
   ["FILE" [:padded-digits 9999]]
   ["GOPR" [:padded-digits 9999]]
   ["GP01" [:padded-digits 9999]]
   ["GX01" [:padded-digits 9999]]
   ["DJI " [:padded-digits 2000]]
   ["HNI 0" [:padded-digits 100]]
   ["WA0" [:padded-digits 999]]
   ["MOL0" [:random-character "ABCDEF"]  [:random-integer 9]]
   ["MOL0" [:padded-digits 99]]
   [[:random-hhmmss]]
   ["P100" [:padded-digits 1999]]
   ["VTS " [:padded-digits 99] " " [:random-integer 9]]
   ["VTS " [:padded-digits 999] " 1"]
   ["VTS 01 " [:padded-digits 999]]
   ["\"My Slideshow Video\""]
   ["\"My Slideshow\""]
   ["\"My Slideshow " [:padded-digits 99] "\""]
   ["\"My Stupeflix Video\""]
   ["\"My Stupeflix Video " [:padded-digits 9999] "\""]
   [[:random-yyyyMMdd {}]]
   ["WIN " [:random-yyyyMMdd {:start-year 2013}]]
   ["VID " [:random-yyyyMMdd {:start-year 2008}]]
   ["Capture " [:random-yyyyMMdd {:start-year 2008}]]
   ["InShot " [:random-yyyyMMdd {:start-year 2016}]]
   ["PXL " [:random-yyyyMMdd {:start-year 2020}]]
   ["AUD-" [:random-yyyyMMdd {:start-year 2017}]]
   ["WhatsApp Video " [:random-yyyyMMdd {:format "yyyy MM dd"
                                         :start-year 2015}]]
   ["Desktop " [:random-yyyyMMdd {:format "yyyy MM dd"}]]

   ["Video" [:padded-digits 9999]]
   ["Trim 4" [:padded-digits 999]]
   ["M2U0" [:padded-digits 9999]]
   ["AVSEQ" [:padded-digits 99]]
   ])

(comment
  (map
    (fn [f]
      (println f)
      (generate-string f))
    forgotten-videos)
  )

;; TODO some search terms need the query parameter "&sp=CAISAhAB" to sort by upload date. How to do?
(def new-videos
  [["IMG"]
   ["MVI"]
   [[:random-yyyyMMdd {}]]
   ["WIN " [:random-yyyyMMdd {}]]
   ["Capture " [:random-yyyyMMdd {}]]
   ["VID " [:random-yyyyMMdd {}]]
   ["\"My Movie " [:random-integer 100] "\""]
   ["\"My Edited Video\""]
   ["/Storage/Emulated/"]
   ["PXL"]
   ["InShot " [:random-yyyyMMdd {}]]
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
   ["YouCut " [:random-yyyyMMdd {}]]
   ["\"Video " [:random-yyyyMMdd {}] "\""]
   ["\"Copy of Copy of\""]
   ["\"Untitled video\""]
   ["\"YTPMV\""]
   ["\"Klasky Csupo\""]
   ["\"Com Oculus Vrshell\""]
   ["\"Com Oculus Metacam\""]
   ["Desktop " [:random-yyyyMMdd {:format "yyyy MM dd"}]]
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

(defn random-forgotten-search-query
  []
  (generate-string (rand-nth forgotten-videos))
  )

(comment
  (random-forgotten-search-query)

  (generate-string ["Desktop " [:random-yyyyMMdd {:format "yyyy MM dd"}]])
  )
