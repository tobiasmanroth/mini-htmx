(ns mini-htmx.layout
  (:require [hiccup2.core :as h]))

(defn layout [title & body]
  (str (h/html
         [:html
          [:head
           [:meta {:charset "UTF-8"}]
           [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
           [:title title]
           [:link {:rel "icon" :type "image/svg+xml"
                   :href "data:image/svg+xml,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 64 64'><rect x='2' y='10' width='60' height='44' rx='12' fill='%23FF0000'/><text x='32' y='40' font-family='Arial,sans-serif' font-weight='bold' font-size='26' fill='white' text-anchor='middle'>nv</text></svg>"}]
           ;; PicoCSS
           [:link {:rel "stylesheet"
                   :href "https://cdn.jsdelivr.net/npm/@picocss/pico@2/css/pico.min.css"}]
           ;; HTMX
           [:script {:src "https://unpkg.com/htmx.org@2.0.4"}]
           [:script {:src "https://cdn.jsdelivr.net/npm/spin-wheel@5.0.2/dist/spin-wheel-iife.js"}]
           [:script {:src "https://unpkg.com/alpinejs@3/dist/cdn.min.js"
                     :defer "true"}]

           [:style "
              #wheel-wrapper {
                position: relative;
                width: 100%;
                max-width: 700px;
                margin: 0 auto;
              }
              #wheel-container {
                width: 100%;
                aspect-ratio: 1;
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
