// =============================================================================
// Express.js Tutorial Server
// =============================================================================
// This file is the main entry point for our Express.js tutorial server.
// It demonstrates how to create a simple web server with multiple routes
// using the Express.js framework on Node.js.
// =============================================================================

// ---------------------------------------------------------------------------
// Section 1: Module Import
// ---------------------------------------------------------------------------
// We use Node.js's built-in CommonJS module system to import Express.
// The `require()` function loads the Express package from node_modules/,
// which was installed via `npm install` based on the dependency declared
// in package.json.
const express = require('express');

// ---------------------------------------------------------------------------
// Section 2: Application Initialization
// ---------------------------------------------------------------------------
// Calling `express()` creates a new Express application instance.
// This `app` object is the central piece of our server — it holds all
// configuration, middleware, and route definitions. Think of it as the
// "control center" that coordinates incoming HTTP requests with the
// appropriate handler functions.
const app = express();

// ---------------------------------------------------------------------------
// Section 3: Port Configuration
// ---------------------------------------------------------------------------
// We define the port number on which our server will listen for requests.
//
// The `process.env.PORT` expression reads the PORT environment variable,
// which allows deployment platforms (like Heroku, Railway, or Render) to
// assign a port dynamically. If the environment variable is not set (e.g.,
// during local development), we fall back to port 3000 as a sensible default.
//
// Usage examples:
//   - Local development:  node index.js          → listens on port 3000
//   - Custom port:        PORT=8080 node index.js → listens on port 8080
const PORT = process.env.PORT || 3000;

// ---------------------------------------------------------------------------
// Section 4: Route Handler — GET /
// ---------------------------------------------------------------------------
// This route handler responds to HTTP GET requests at the root URL ("/").
//
// `app.get(path, handler)` registers a route that only matches GET requests.
// When a client (browser, curl, Postman) sends a GET request to "/", Express
// calls our handler function with two arguments:
//   - `req` (request)  — contains information about the incoming request
//   - `res` (response) — provides methods to send a response back to the client
//
// `res.send()` sends the provided string as the HTTP response body and
// automatically sets the Content-Type header to "text/html" for strings.
app.get('/', (req, res) => {
  res.send('Hello world');
});

// ---------------------------------------------------------------------------
// Section 5: Route Handler — GET /evening
// ---------------------------------------------------------------------------
// This route handler responds to HTTP GET requests at the "/evening" URL.
//
// It follows the same pattern as the root route above, but serves a
// different greeting. This demonstrates how Express makes it easy to
// define multiple endpoints — each with its own URL path and response.
//
// To test this endpoint:
//   - Browser:  http://localhost:3000/evening
//   - curl:     curl http://localhost:3000/evening
app.get('/evening', (req, res) => {
  res.send('Good evening');
});

// ---------------------------------------------------------------------------
// Section 6: Server Binding
// ---------------------------------------------------------------------------
// `app.listen(port, callback)` starts the HTTP server and binds it to the
// specified port. Once the server is ready to accept connections, Express
// calls the callback function — here, we log a helpful message showing
// the exact URL where the server is accessible.
//
// The server will continue running until you stop it (Ctrl+C in the terminal).
app.listen(PORT, () => {
  console.log(`Server is running on http://localhost:${PORT}`);
});
