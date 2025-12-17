(ns mini-htmx.core
  (:require [org.httpkit.server :as http]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.util.response :as response]
            [hiccup2.core :as h]
            [hiccup.page :refer [html5]]
            [nrepl.server :as nrepl]))

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
                  :href "https://cdn.jsdelivr.net/npm/picnic@7.1.0/picnic.min.css"}]
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

          [:h2 "HTMX Counter Demo"]
          [:div
           [:p "Current count: " [:strong#count @counter]]
           [:div.button-group
            [:button {:hx-post "/increment"
                      :hx-target "#count"
                      :hx-swap "innerHTML"
                      :class "button"}
             "Increment"]
            [:button {:hx-post "/decrement"
                      :hx-target "#count"
                      :hx-swap "innerHTML"
                      :class "button"}
             "Decrement"]
            [:button {:hx-post "/reset"
                      :hx-target "#count"
                      :hx-swap "innerHTML"
                      :class "button"}
             "Reset"]]]

          [:hr]

          [:h2 "HTMX Form Demo"]
          [:form {:hx-post "/greet"
                  :hx-target "#greeting"
                  :hx-swap "innerHTML"}
           [:label "Enter your name:"]
           [:input {:type "text"
                    :name "name"
                    :placeholder "Your name"
                    :required true}]
           [:button {:type "submit" :class "button"} "Greet Me"]]
          [:div#greeting]))

(defn increment-handler [_]
  (let [new-count (swap! counter inc)]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (str new-count)}))

(defn decrement-handler [_]
  (let [new-count (swap! counter dec)]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (str new-count)}))

(defn reset-handler [_]
  (reset! counter 0)
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "0"})

(defn greet-handler [request]
  (let [params (-> request :params)
        name (get params "name" "Stranger")]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (str (h/html [:p {:class "success"} "Hello, " [:strong name] "! 👋"]))}))

(defn routes [request]
  (let [uri (:uri request)
        method (:request-method request)]
    (cond
      (and (= uri "/") (= method :get))
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (index-page)}

      (and (= uri "/increment") (= method :post))
      (increment-handler request)

      (and (= uri "/decrement") (= method :post))
      (decrement-handler request)

      (and (= uri "/reset") (= method :post))
      (reset-handler request)

      (and (= uri "/greet") (= method :post))
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
  (reset! nrepl-server (nrepl/start-server :port 4000))
  (println "nREPL server started on port 4000"))

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
               3000)]
    (start-server! port)))
