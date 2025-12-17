# Mini HTMX - Clojure Web Application

A simple Clojure web application demonstrating http-kit, Ring, Hiccup, HTMX, and PicoCSS.

## Features

- **http-kit**: Fast, lightweight web server
- **Ring**: Standard web application library
- **Hiccup**: HTML generation using Clojure data structures
- **HTMX**: Dynamic HTML without writing JavaScript
- **PicoCSS**: Minimal CSS framework for clean styling

## Demo Features

1. **Counter Demo**: Increment, decrement, and reset a counter using HTMX
2. **Greeting Form**: Submit a name and get a personalized greeting
3. **Clean UI**: Styled with PicoCSS

## Running the Application

```bash
# Start the server on default port 3000
clj -M:run

# Or specify a custom port
clj -M:run 8080
```

Then open your browser to: http://localhost:3000

An nREPL server will also start automatically on port 4000 for remote REPL connections.

## Project Structure

```
mini-htmx/
├── deps.edn              # Dependencies
├── src/
│   └── mini_htmx/
│       └── core.clj      # Main application
└── README.md
```

## Development

When you start the application, an nREPL server automatically starts on port 4000. You can connect to it from your editor:

**Emacs/CIDER:**
```
M-x cider-connect RET localhost RET 4000
```

**VS Code/Calva:**
```
Ctrl+Alt+C Ctrl+Alt+C → Connect to a running REPL → localhost:4000
```

**IntelliJ/Cursive:**
```
Run → Edit Configurations → + → Clojure REPL → Remote → localhost:4000
```

The server uses `#'app` var for the handler, so you can reload code at the REPL without restarting:

```clojure
;; Connect to nREPL on port 4000, then:
(require '[mini-htmx.core :as app])

;; Make changes to code, then reload
(require '[mini-htmx.core :as app] :reload)

;; The changes will be live without restarting the server
```
