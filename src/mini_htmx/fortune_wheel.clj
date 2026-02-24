(ns mini-htmx.fortune-wheel
  (:require [hiccup2.core :as h]
            [clojure.data.json :as json]
            [mini-htmx.youtube-recycle-bin]
            [mini-htmx.layout :refer [layout]]))

(defn fortune-wheel-panel []
  (let [sorted-videos (sort-by :format-string mini-htmx.youtube-recycle-bin/forgotten-videos)]
    [:div {:x-data "{ allChecked: false,
                       toggle() {
                         this.allChecked = !this.allChecked;
                         this.$refs.form.querySelectorAll('input[type=checkbox]')
                           .forEach(cb => cb.checked = this.allChecked);
                       },
                       randomTen() {
                         const cbs = [...this.$refs.form.querySelectorAll('input[type=checkbox]')];
                         cbs.forEach(cb => cb.checked = false);
                         const shuffled = cbs.sort(() => Math.random() - 0.5);
                         shuffled.slice(0, 10).forEach(cb => cb.checked = true);
                         this.allChecked = false;
                       },
                       init() { this.$nextTick(() => this.randomTen()); }
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
                  :x-on:click "randomTen()"}
         "Random 10"]]
       [:div {:style "columns: 18rem; column-gap: 2rem;"}
        (for [video sorted-videos]
          [:label {:style "display: flex; align-items: center; gap: 0.3rem; margin: 0; white-space: nowrap;"}
           [:input {:type "checkbox"
                    :name "f"
                    :value (:format-string video)}]
           (:format-string video)])]]
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

(defn page
  [items]
  (let [items-json (json/write-str (mapv (fn [item]
                                           {:formatString (:format-string item)
                                            :formatter (:format-fn item)})
                                         items))]
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

                    this.wheel = new spinWheel.Wheel(container, {
                      isInteractive: false,
                      items: this.items.map(item => ({label: item.formatString})),
                      itemBackgroundColors: ['#c7160c', '#fff95b', '#2195f2', '#4caf50', '#ff9800', '#9c27b0'],
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
