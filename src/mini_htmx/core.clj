(ns mini-htmx.core
  (:require [org.httpkit.server :as http]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.util.codec]
            [hiccup2.core :as h]
            [nrepl.server :as nrepl]
            [clojure.string]
            [mini-htmx.youtube-recycle-bin]
            [clojure.data.json :as json])
  (:gen-class))

(defn layout [title & body]
  (str (h/html
         [:html
          [:head
           [:meta {:charset "UTF-8"}]
           [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
           [:title title]
           ;; PicoCSS
           [:link {:rel "stylesheet"
                   :href "https://cdn.jsdelivr.net/npm/@picocss/pico@2/css/pico.min.css"}]
           ;; HTMX
           [:script {:src "https://unpkg.com/htmx.org@2.0.4"}]
           [:script {:src "https://cdn.jsdelivr.net/npm/spin-wheel@5.0.2/dist/spin-wheel-iife.js"}]
           [:script {:src "//unpkg.com/alpinejs"
                     :defer "true"}]

           [:style "
              #wheel-wrapper {
                position: relative;
                width: 500px;
                margin: 0 auto;
              }
              #wheel-container {
                width: 500px;
                height: 500px;
              }
              .wheel-pointer {
                position: absolute;
                top: -20px;
                left: 50%;
                transform: translateX(-50%);
                width: 0;
                height: 0;
                border-left: 10px solid transparent;
                border-right: 10px solid transparent;
                border-top: 70px solid #ccc;
                z-index: 10;
                filter: drop-shadow(0 0px 4px rgba(0,0,0,1.0));
              }
            "]
           ]
          [:body
           [:main.container body]]])))

(defn index-page []
  (layout "Mini HTMX App"
          [:h1 "Welcome to Mini HTMX!"]
          [:p "This is a simple Clojure web application using:"]
          [:ul
           [:li "http-kit (web server)"]
           [:li "Ring (request handling)"]
           [:li "Hiccup (HTML generation)"]
           [:li "HTMX (dynamic interactions)"]
           [:li "PicoCSS (styling)"]]


          [:hr]
          [:h2 "Find forgotten YouTube videos"]
          [:form {:hx-post "/random-forgotten-youtube-link"
                  :hx-target "#random-forgotten-youtube-link"
                  :hx-swap "innerHTML"}
           [:button {:type "submit" :class "button"} "Generate a random YouTube link"]]
          [:div {:id "random-forgotten-youtube-link"}]

          [:hr]
          [:h2 "Fortune Wheel"]
          [:p "Try your luck with the fortune wheel!"]
          [:a {:href "/fortune-wheel"} [:button "Go to Fortune Wheel"]]))


(defn forgotten-youtube-link-handler [request]
  (let [search-query (mini-htmx.youtube-recycle-bin/random-forgotten-search-query)
        encoded-query (java.net.URLEncoder/encode search-query "UTF-8")
        link (str "https://youtube.com/results?search_query=" encoded-query)]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (str (h/html [:a {:href link
                             :target "_blank"} link]))}))

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

(defn fortune-wheel-spin-handler [request]
  (let [body (slurp (:body request))
        params (json/read-str body :key-fn keyword)
        formatter (:formatter params)
        ;; Convert string function names back to keywords
        formatter (convert-formatter-keywords formatter)
        search-query (mini-htmx.youtube-recycle-bin/generate-string formatter)
        encoded-query (java.net.URLEncoder/encode search-query "UTF-8")
        link (str "https://youtube.com/results?search_query=" encoded-query)]
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (json/write-str {:link link})}))

(defn fortune-wheel-page []
  (let [items (vec (take 10 (shuffle mini-htmx.youtube-recycle-bin/forgotten-videos)))
        ;; Convert items to JSON with both format strings and formatters
        items-json (json/write-str (mapv (fn [item]
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

(defn routes [request]
  (let [uri (:uri request)
        method (:request-method request)]
    (cond
      (and (= uri "/") (= method :get))
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (index-page)}

      (and (= uri "/random-forgotten-youtube-link") (= method :post))
      (forgotten-youtube-link-handler request)

      (and (= uri "/fortune-wheel") (= method :get))
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (fortune-wheel-page)}

      (and (= uri "/fortune-wheel-spin") (= method :post))
      (fortune-wheel-spin-handler request)

      :else
      {:status 404
       :headers {"Content-Type" "text/html"}
       :body (layout "404 Not Found"
                     [:h1 "404 - Page Not Found"]
                     [:p "The page you're looking for doesn't exist."]
                     [:a {:href "/"} "Go back home"])})))

(def app
  (wrap-defaults routes
                 (assoc-in site-defaults
                           [:security :anti-forgery]
                           false)))

(defonce server (atom nil))
(defonce nrepl-server (atom nil))

(defn start-server! [port]
  (when @server
    (@server))
  (reset! server (http/run-server #'app {:port port}))
  (println (str "Server started on http://localhost:" port))

  (when @nrepl-server
    (nrepl/stop-server @nrepl-server))
  (reset! nrepl-server (nrepl/start-server :port 8282))
  (println "nREPL server started on port 8282"))

(defn stop-server! []
  (when @server
    (@server :timeout 100)
    (reset! server nil)
    (println "Server stopped"))

  (when @nrepl-server
    (nrepl/stop-server @nrepl-server)
    (reset! nrepl-server nil)
    (println "nREPL server stopped")))

(defn -main [& args]
  (let [port (if (first args)
               (Integer/parseInt (first args))
               8181)]
    (start-server! port)))
