(ns mini-htmx.core
  (:require [org.httpkit.server :as http]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [hiccup2.core :as h]
            [hiccup.page :refer [html5]]
            [nrepl.server :as nrepl]
            [mini-htmx.youtube-recycle-bin]))

(def counter (atom 0))

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
          [:script {:src "https://unpkg.com/htmx.org@2.0.4"}]]
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
          [:h2 "Random YouTube Link Demo"]
          [:form {:hx-post "/random-youtube-link"
                  :hx-target "#random-youtube-link"
                  :hx-swap "innerHTML"}
           [:button {:type "submit" :class "button"} "Generate a random YouTube link"]]
          [:div {:id "random-youtube-link"}]))


(defn greet-handler [request]
  (let [search-query (mini-htmx.youtube-recycle-bin/random-search-query)
        link (str "https://youtube.com/results?search_query=" search-query)]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (str (h/html [:a {:href link
                             :target "_blank"} link]))}))

(defn routes [request]
  (let [uri (:uri request)
        method (:request-method request)]
    (cond
      (and (= uri "/") (= method :get))
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (index-page)}

      (and (= uri "/random-youtube-link") (= method :post))
      (greet-handler request)

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
