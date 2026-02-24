(ns mini-htmx.fortune-wheel
  (:require [hiccup2.core :as h]
            [clojure.data.json :as json]
            [mini-htmx.youtube-recycle-bin]
            [mini-htmx.layout :refer [layout]]))

(def color-sets
  [["classic" ["#c7160c" "#fff95b" "#2195f2" "#4caf50" "#ff9800" "#9c27b0"]]
   ["pastel"  ["#FFB3BA" "#FFDFBA" "#FFFFBA" "#BAFFC9" "#BAE1FF" "#E8BAFF"]]
   ["neon"    ["#FF6EC7" "#39FF14" "#FF073A" "#04D9FF" "#FFFF00" "#FF4500"]]
   ["mono"    ["#222222" "#444444" "#666666" "#888888" "#AAAAAA" "#CCCCCC"]]
   ["ocean"   ["#003B46" "#07575B" "#66A5AD" "#C4DFE6" "#1B4F72" "#5DADE2"]]
   ["sunset"  ["#FF6B35" "#F7C59F" "#EFEFD0" "#004E89" "#1A659E" "#FF4365"]]
   ["forest"  ["#2D5F2D" "#4A7C59" "#8FBC8F" "#D4A574" "#6B4226" "#A0522D"]]
   ["candy"   ["#FF69B4" "#FF1493" "#FFD700" "#00CED1" "#FF6347" "#9370DB"]]])

(defn fortune-wheel-panel []
  (let [sorted-videos (sort-by :format-string mini-htmx.youtube-recycle-bin/forgotten-videos)]
    [:div {:x-data "{ allChecked: false,
                       toggle() {
                         this.allChecked = !this.allChecked;
                         this.$refs.form.querySelectorAll('input[type=checkbox]')
                           .forEach(cb => cb.checked = this.allChecked);
                       },
                       randomFormatters (n) {
                         const cbs = [...this.$refs.form.querySelectorAll('input[type=checkbox]')];
                         cbs.forEach(cb => cb.checked = false);
                         const shuffled = cbs.sort(() => Math.random() - 0.5);
                         shuffled.slice(0, n).forEach(cb => cb.checked = true);
                         this.allChecked = false;
                       },
                       init() { this.$nextTick(() => this.randomFormatters(12)); }
                     }"}
     [:form {:method "get" :action "/fortune-wheel" :target "_blank" :x-ref "form"}
      [:fieldset
       [:legend "Select formatters to include:"]
       [:div {:style "display: flex; gap: 0.5rem; margin-bottom: 1rem;"}
        [:button {:type "button"
                  :class "outline secondary"
                  :x-on:click "toggle()"}
         [:span {:x-text "allChecked ? 'Deselect All' : 'Select All'"}]]
        [:button {:type "button"
                  :class "outline secondary"
                  :x-on:click "randomFormatters(12)"}
         "Random 12"]]
       [:div {:style "columns: 18rem; column-gap: 2rem;"}
        (for [video sorted-videos]
          [:label {:style "display: flex; align-items: center; gap: 0.3rem; margin: 0; white-space: nowrap;"}
           [:input {:type "checkbox"
                    :name "f"
                    :value (:format-string video)}]
           (:format-string video)])]]
      [:fieldset
       [:legend "Color theme:"]
       [:div {:style "display: flex; gap: 1.5rem; flex-wrap: wrap;"}
        (for [[idx [name colors]] (map-indexed vector color-sets)]
          [:label {:style "display: flex; align-items: center; gap: 0.3rem; margin: 0;"}
           [:input {:type "radio"
                    :name "colors"
                    :value name
                    :checked (= idx 0)}]
           [:span {:style "display: inline-flex; gap: 2px; margin-left: 0.3rem;"}
            (for [c colors]
              [:span {:style (str "display: inline-block; width: 14px; height: 14px; background:" c "; border-radius: 2px;")}])]])]]
      [:button {:type "submit"} "Go to Fortune Wheel"]]]))

(defn convert-formatter-keywords
  "Converts string function names to keywords in formatter data structure"
  [formatter]
  (mapv (fn [item]
          (if (vector? item)
            (let [func-name (first item)]
              (if (string? func-name)
                (vec (cons (keyword func-name) (rest item)))
                item))
            item))
        formatter))

(defn spin-handler [request]
  (let [body (slurp (:body request))
        params (json/read-str body :key-fn keyword)
        formatter (:formatter params)
        formatter (convert-formatter-keywords formatter)
        search-query (mini-htmx.youtube-recycle-bin/generate-string formatter)
        encoded-query (java.net.URLEncoder/encode search-query "UTF-8")
        link (str "https://youtube.com/results?search_query=" encoded-query)]
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (json/write-str {:link link})}))

(def ^:private color-map (into {} color-sets))
(def ^:private default-colors (second (first color-sets)))

(defn page
  [items color-name]
  (let [items-json (json/write-str (mapv (fn [item]
                                           {:formatString (:format-string item)
                                            :formatter (:format-fn item)})
                                         items))
        colors (get color-map color-name default-colors)
        colors-json (json/write-str colors)]
    (layout "Fortune Wheel - Forgotten Videos"
            [:article {:x-data (str "{
                items: " items-json ",
                wheel: null,
                resultLink: '',
                isSpinning: false,

                init() {
                  this.$nextTick(() => {
                    const container = this.$refs.wheelContainer;
                    if (!container) {
                      console.error('Wheel container not found!');
                      return;
                    }

                    const bgColors = " colors-json ";
                    const labelColors = bgColors.map(c => {
                      const r = parseInt(c.slice(1,3), 16);
                      const g = parseInt(c.slice(3,5), 16);
                      const b = parseInt(c.slice(5,7), 16);
                      return (r * 0.299 + g * 0.587 + b * 0.114) > 150 ? '#000' : '#fff';
                    });
                    this.wheel = new spinWheel.Wheel(container, {
                      isInteractive: false,
                      items: this.items.map(item => ({label: item.formatString})),
                      itemBackgroundColors: bgColors,
                      itemLabelColors: labelColors,
                      borderWidth: 5,
                      borderColor: '#000',
                    });
                  });
                },

                async spinWheel() {
                  if (this.isSpinning) return;

                  this.isSpinning = true;
                  this.resultLink = '';

                  // Client-side random selection
                  const randomIndex = Math.floor(Math.random() * this.items.length);
                  const selectedItem = this.items[randomIndex];

                  // Spin the wheel to the client-selected index
                  this.wheel.spinToItem(randomIndex, 3000, true, 2, 1);

                  try {
                    // Ask server to generate link for the selected formatter
                    const response = await fetch('/fortune-wheel-spin', {
                      method: 'POST',
                      headers: {
                        'Content-Type': 'application/json',
                      },
                      body: JSON.stringify({formatter: selectedItem.formatter})
                    });

                    const data = await response.json();

                    // After spinning completes, display the result
                    setTimeout(() => {
                      this.resultLink = data.link;
                      this.isSpinning = false;
                    }, 3000);
                  } catch (error) {
                    console.error('Error spinning wheel:', error);
                    this.resultLink = 'Error: ' + error.message;
                    this.isSpinning = false;
                  }
                }
              }")}
             [:h1 "Fortune Wheel"]
             [:p "Spin the wheel to discover a random forgotten video search query!"]
             [:div {:id "wheel-wrapper"}
              [:div {:class "wheel-pointer"}]
              [:div {:x-ref "wheelContainer"
                     :id "wheel-container"}]]
             [:div {:style "text-align: center;"}
              [:button {:x-on:click "spinWheel()"
                        :x-bind:disabled "isSpinning"}
               "Spin the Wheel!"]
              [:br]
              [:br]
              [:a {:x-ref "result"
                   :x-bind:href "resultLink"
                   :x-text "resultLink"
                   :target "_blank"}]]]
            )))
