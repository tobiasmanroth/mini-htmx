(ns mini-htmx.youtube-recycle-bin)

(defn digit-count
  [n]
  (count (str (Math/abs n))))

(defn random-padded-int
  "inclusive max"
  ([min max]
   (format (str "%0" (digit-count max) "d")
           (+ min (rand-int (- (inc max) min)))))
  ([max]
   (random-padded-int 0 max)))

(defn random-yyyyMMdd
  [{:keys [format start-year end-year]
    :or {format "yyyyMMdd"}}]
  (let [youtube-founded (java.time.LocalDate/of 2005 2 14)
        start-date (if start-year
                     (java.time.LocalDate/of start-year 1 1)
                     youtube-founded)
        end-date (or (when end-year (java.time.LocalDate/of end-year 12 31))
                     (java.time.LocalDate/now))
        days-between (.between java.time.temporal.ChronoUnit/DAYS start-date end-date)
        random-days (rand-int (inc days-between))
        random-date (.plusDays start-date random-days)
        formatter (java.time.format.DateTimeFormatter/ofPattern format)]
    (.format random-date formatter)))

(comment
  (random-yyyyMMdd {:format "MMMM dd, yyyy"})
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

(defn random-padded-hex
  "Generates a random hex number up to the max defined by the input string.
   For example, (random-hex \"FFF\") returns a random hex string from \"000\" to \"FFF\"."
  [max-hex-str]
  (let [max-value (Integer/parseInt max-hex-str 16)
        random-value (rand-int (inc max-value))
        padding (count max-hex-str)
        format-str (str "%0" padding "X")]
    (format format-str random-value)))

(comment
  (random-padded-hex "FFF")
  (random-padded-hex "FF")
  (random-padded-hex "FFFF")
  )

(def registry
  {:padded-digits #'random-padded-int
   :random-yyyyMMdd #'random-yyyyMMdd
   :random-integer #'rand-int
   :random-yyyy #'random-yyyy
   :random-character #'random-character
   :random-hhmmss #'random-hhmmss
   :random-padded-hex #'random-padded-hex})

(def forgotten-videos
  #{{:format-fn ["DJI " [:padded-digits 2000]]
     :format-string "DJI ####"}
    {:format-fn ["Capture " [:random-yyyyMMdd {:start-year 2008}]]
     :format-string "Capture yyyyMMdd"}
    {:format-fn ["PICT" [:padded-digits 9999]]
     :format-string "PICT####"}
    {:format-fn ["PXL " [:random-yyyyMMdd {:start-year 2020}]]
     :format-string "PXL yyyyMMdd"}
    {:format-fn ["WIN " [:random-yyyyMMdd {:start-year 2013}]]
     :format-string "WIN yyyyMMdd"}
    {:format-fn ["AVSEQ" [:padded-digits 99]]
     :format-string "AVSEQ##"}
    {:format-fn ["MVI " [:padded-digits 9999]]
     :format-string "MVI ####"}
    {:format-fn ["Desktop " [:random-yyyyMMdd {:format "yyyy MM dd"}]]
     :format-string "Desktop yyyy MM dd"}
    {:format-fn ["\"My Stupeflix Video\""]
     :format-string "\"My Stupeflix Video\""}
    {:format-fn ["YouCut " [:random-yyyyMMdd {:start-year 2012}]]
     :format-string "YouCut yyyyMMdd"}
    {:format-fn ["Video" [:padded-digits 9999]]
     :format-string "Video####"}
    {:format-fn [[:random-hhmmss]]
     :format-string "HHMMSS"}
    {:format-fn ["GX01" [:padded-digits 9999]]
     :format-string "GX01####"}
    {:format-fn ["GP01" [:padded-digits 9999]]
     :format-string "GP01####"}
    {:format-fn ["IMG " [:padded-digits 9999]]
     :format-string "IMG ####"}
    {:format-fn ["WA0" [:padded-digits 999]]
     :format-string "WA0###"}
    {:format-fn ["VTS " [:padded-digits 99] " " [:random-integer 9]]
     :format-string "VTS ## #"}
    {:format-fn ["720p " [:random-yyyyMMdd {:format "yyMMdd", :start-year 2023}]]
     :format-string "720p yyMMdd"}
    {:format-fn ["Javaw " [:random-yyyyMMdd {:format "yyyy MM dd", :start-year 2009}]]
     :format-string "Javaw yyyy MM dd"}
    {:format-fn ["\"My Stupeflix Video " [:padded-digits 9999] "\""]
     :format-string "\"My Stupeflix Video ####\""}
    {:format-fn ["SAM " [:padded-digits 9999]]
     :format-string "SAM ####"}
    {:format-fn ["MAQ0" [:padded-digits 9999]]
     :format-string "MAQ0####"}
    {:format-fn ["FILE" [:padded-digits 9999]]
     :format-string "FILE####"}
    {:format-fn ["Chrome " [:random-yyyyMMdd {:format "yyyy MM dd", :start-year 2010}]],
     :format-string "Chrome yyyy MM dd"}
    {:format-fn ["GOPR" [:padded-digits 9999]]
     :format-string "GOPR####"}
    {:format-fn ["Bandicam " [:random-yyyyMMdd {:format "yyyy MM dd"
                                                :start-year 2010}]]
     :format-string "Bandicam yyyy MM dd"}
    {:format-fn ["MOL0" [:padded-digits 99]]
     :format-string "MOL0##"}
    {:format-fn ["HNI 0" [:padded-digits 100]]
     :format-string "HNI 0###"}
    {:format-fn ["DSCN" [:padded-digits 9999]]
     :format-string "DSCN####"}
    {:format-fn ["VID0" [:padded-digits 10]]
     :format-string "VID0##"}
    {:format-fn ["M2U0" [:padded-digits 9999]]
     :format-string "M2U0####"}
    {:format-fn ["InShot " [:random-yyyyMMdd {:start-year 2016}]]
     :format-string "InShot yyyyMMdd"}
    {:format-fn ["XRecorder " [:random-yyyyMMdd {:format "ddMMyyyy", :start-year 2021, :end-year 2024}]]
     :format-string "XRecorder ddMMyyyy"}
    {:format-fn ["Km " [:random-yyyyMMdd {:start-year 2021}]]
     :format-string "Km yyyyMMdd"}
    {:format-fn ["Simplescreenrecorder " [:random-yyyyMMdd {:format "yyyy MM dd", :start-year 2023}]]
     :format-string "Simplescreenrecorder yyyy MM dd"}
    {:format-fn ["Trim 4" [:random-padded-hex "FFF"]]
     :format-string "Trim 4FFF"}
    {:format-fn ["DSCF" [:padded-digits 9999]]
     :format-string "DSCF####"}
    {:format-fn ["MAH0" [:padded-digits 9999]]
     :format-string "MAH0####"}
    {:format-fn ["VID " [:random-yyyyMMdd {:start-year 2008}]]
     :format-string "VID yyyyMMdd"}
    {:format-fn ["P100" [:padded-digits 1999]]
     :format-string "P100####"}
    {:format-fn ["CODWAWMP " [:random-yyyyMMdd {:format "yyyy MM dd", :start-year 2008, :end-year 2023}]]
     :format-string "CODWAWMP yyyy MM dd"}
    {:format-fn ["SDV " [:padded-digits 9999]]
     :format-string "SDV ####"}
    {:format-fn ["WhatsApp Video " [:random-yyyyMMdd {:format "yyyy MM dd", :start-year 2015}]],
     :format-string "WhatsApp Video yyyy MM dd"}
    {:format-fn ["Grand Theft Auto 5 " [:random-yyyyMMdd {:format "yyyy MM dd", :start-year 2008}]],
     :format-string "Grand Theft Auto 5 yyyy MM dd"}
    {:format-fn ["Hl2 " [:random-yyyyMMdd {:format "yyyy MM dd", :start-year 2008, :end-year 2023}]],
     :format-string "Hl2 yyyy MM dd"}
    {:format-fn ["DSC " [:padded-digits 9999]]
     :format-string "DSC ####"}
    {:format-fn ["WA VID " [:random-yyyyMMdd {:start-year 2018, :end-year 2023}]]
     :format-string "WA VID yyyyMMdd"}
    {:format-fn ["MOL0" [:random-character "ABCDEF"] [:random-integer 9]]
     :format-string "MOL0X#"}
    {:format-fn ["VTS 01 " [:padded-digits 999]]
     :format-string "VTS 01 ###"}
    {:format-fn ["AUD-" [:random-yyyyMMdd {:start-year 2017}]]
     :format-string "AUD-yyyyMMdd"}
    {:format-fn ["VTS " [:padded-digits 999] " 1"]
     :format-string "VTS ### 1"}
    {:format-fn ["MOV " [:padded-digits 9999]]
     :format-string "MOV ####"}
    {:format-fn ["100 " [:padded-digits 9999]]
     :format-string "100 ####"}
    {:format-fn [[:random-yyyyMMdd {}]]
     :format-string "yyyyMMdd"}
    {:format-fn ["\"My Slideshow " [:padded-digits 99] "\""]
     :format-string "\"My Slideshow ##\""}
    {:format-fn ["MOV000" [:padded-digits 10]]
     :format-string "MOV000##"}
    {:format-fn ["\"My Slideshow Video\""]
     :format-string "\"My Slideshow Video\""}
    {:format-fn ["\"My Slideshow\""]
     :format-string "\"My Slideshow\""}
    {:format-fn ["XRecorder " [:random-yyyyMMdd {:start-year 2024}]]
     :format-string "XRecorder yyyyMMdd"}})

(def low-views-2006-2008
  [["\"You have new picture mail! (video)\""]
   ["\"Media1.3gp\""]
   ["\"Media1.3g2\""]
   ["\"Video.3g2\""]
   ["\"New Multimedia Message\""]
   ["\"Multimedia Message\""]
   ["\"Video from my phone\""]
   ["\"Video uploaded from my mobile phone\""]
   ["\"For " [:random-yyyyMMdd {:format "MMMM dd, yyyy"
                                :start-year 2006
                                :end-year 2008}] "\""]
   ["\"Recorded on " [:random-yyyyMMdd {:format "MMMM dd, yyyy"
                                        :start-year 2006
                                        :end-year 2008}]
    " using a Flip Video Camcorder\""]
   ["Video0" [:padded-digits 10]]
   ["Vid0" [:padded-digits 10]]
   ["MOV000" [:padded-digits 10]]
   ["\"Recorded on " [:random-yyyyMMdd {:format "MMMM dd, yyyy"
                                        :start-year 2006
                                        :end-year 2008}]
    " using a Flip Video Camera\""]
   ["\"Created on " [:random-yyyyMMdd {:format "MMMM dd, yyyy"
                                       :start-year 2006
                                       :end-year 2008}]
    " using FlipShare\""]
   ["\"Video van Mijn telefoon\""]
   ["\"video geüpload van mijn mobiel\""]
   ["\"Vídeo desde mi teléfono\""]
   ["\"vídeo subido desde mi teléfono móvil\""]
   ["muuvee00" [:padded-digits 40]]
   ["0_VIDEO_0" [:padded-digits 54]]
   ["\"You have received a new message\""]
   ["\"“My Great Movie”\""]
   ["\"My First Project\""]])

(comment
  (map
    (fn [{:keys [format-fn format-string]}]
      (println format-fn)
      (generate-string format-fn))
    forgotten-videos)

  (map
    (fn [f]
      (println f)
      (generate-string f))
    low-views-2006-2008)
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
  (generate-string (:format-fn (rand-nth (vec forgotten-videos))))
  )

(comment
  (random-forgotten-search-query)

  (generate-string ["Recorded on" [:random-yyyyMMdd {:format "MMMM dd, yyyy"}] "using a Flip Video Camera"])
  )
