(ns mini-htmx.core
  (:require [org.httpkit.server :as http]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [hiccup2.core :as h]
            [nrepl.server :as nrepl]
            [mini-htmx.youtube-recycle-bin]
            [mini-htmx.layout :refer [layout]]
            [mini-htmx.fortune-wheel :as fortune-wheel])
  (:gen-class))

(defn index-page []
  (layout "No Views"
          [:h1 "Welcome to no-views.com"]
          [:p "Someone uploaded a video. Nobody watched it. Until now. Maybe."]
          [:p "This page digs up YouTube videos with zero or near-zero views "
           "- the digital equivalent of finding a message in a bottle, "
           "except the message is usually a 2009 cat video filmed on a toaster. "
           "It's a treasure hunt where the treasure is wonderfully questionable."]
          [:p "Still a work in progress - I keep adding weird new ways to find weird old videos."]
          [:p "Inspired by " [:a {:href "https://www.youtube.com/@KVNAUST"}
                               "@KVNAUST"]]
          [:hr]
          [:h2 "Find forgotten YouTube videos"]
          [:form {:hx-post "/random-forgotten-youtube-link"
                  :hx-target "#random-forgotten-youtube-link"
                  :hx-swap "innerHTML"}
           [:button
            {:type "submit" :class "button"}
            "Generate a random YouTube link"]]
          [:div {:id "random-forgotten-youtube-link"}]

          [:hr]
          [:h2 "Fortune Wheel"]
          [:p "Try your luck with the fortune wheel!"]
          (fortune-wheel/fortune-wheel-panel)))


(defn forgotten-youtube-link-handler [request]
  (let [search-query (mini-htmx.youtube-recycle-bin/random-forgotten-search-query)
        encoded-query (java.net.URLEncoder/encode search-query "UTF-8")
        link (str "https://youtube.com/results?search_query=" encoded-query)]
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

      (and (= uri "/random-forgotten-youtube-link") (= method :post))
      (forgotten-youtube-link-handler request)

      (and (= uri "/fortune-wheel") (= method :get))
      (let [f-param (get-in request [:params :f])
            f-set (cond
                    (nil? f-param) nil
                    (string? f-param) #{f-param}
                    :else (set f-param))
            videos (if f-set
                     (vec (filter #(f-set (:format-string %))
                                  mini-htmx.youtube-recycle-bin/forgotten-videos))
                     (vec (take 10 (shuffle mini-htmx.youtube-recycle-bin/forgotten-videos))))
            color-name (get-in request [:params :colors] "classic")]
        {:status 200
         :headers {"Content-Type" "text/html"}
         :body (fortune-wheel/page videos color-name)})

      (and (= uri "/fortune-wheel-spin") (= method :post))
      (fortune-wheel/spin-handler request)

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
