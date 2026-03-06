// index.js — Express.js Tutorial Server Entry Point
// This file creates an HTTP server using Express.js with two GET endpoints:
//   GET /             → returns "Hello world"
//   GET /good-evening → returns "Good evening"

// Import the Express.js framework using CommonJS module syntax
const express = require('express');

// Create an Express application instance
const app = express();

// Define the port number the server will listen on
const PORT = 3000;

// Endpoint returning "Hello world" response
app.get('/', (req, res) => {
  res.send('Hello world');
});

// Endpoint returning "Good evening" response
app.get('/good-evening', (req, res) => {
  res.send('Good evening');
});

// Start the server and listen on the specified port
// The callback function logs a confirmation message once the server is ready
app.listen(PORT, () => {
  console.log(`Server is listening on port ${PORT}`);
});
