# Express.js Tutorial Server

A simple Node.js web server built with [Express.js](https://expressjs.com/) 5.2.1 that demonstrates basic HTTP endpoint routing. This project serves as a beginner-friendly tutorial for learning how to create and configure a Node.js server using the Express.js framework.

The server exposes two GET endpoints that return plain-text greeting responses — a classic starting point for anyone new to server-side JavaScript development.

## Prerequisites

Before you begin, make sure you have the following software installed on your machine:

- **Node.js** — version 18.0.0 or higher
- **npm** — version 8.0.0 or higher (included with Node.js)

You can verify your installed versions by running the following commands in your terminal:

```bash
node --version
```

Expected output: `v18.0.0` or higher (e.g., `v20.20.1`)

```bash
npm --version
```

Expected output: `8.0.0` or higher (e.g., `11.1.0`)

If you need to install or update Node.js, visit the [official Node.js website](https://nodejs.org/) to download the latest LTS release.

## Installation

Follow these steps to set up the project locally:

1. **Clone the repository:**

   ```bash
   git clone <repository-url>
   ```

2. **Navigate into the project directory:**

   ```bash
   cd express-tutorial-server
   ```

3. **Install dependencies:**

   ```bash
   npm install
   ```

   This command reads the `package.json` file, downloads Express.js and its transitive dependencies into the `node_modules/` directory, and generates a `package-lock.json` lockfile for deterministic installations.

## Usage

### Starting the Server

Start the server using the npm start script:

```bash
npm start
```

This runs `node index.js` under the hood, which boots the Express.js server.

### Default Port

By default, the server listens on **port 3000**. Once started, you will see the following confirmation message in your terminal:

```
Server is running on http://localhost:3000
```

### Custom Port Configuration

You can configure the server to listen on a different port by setting the `PORT` environment variable:

```bash
PORT=8080 npm start
```

This starts the server on port 8080 instead of the default 3000. The confirmation message will reflect the custom port:

```
Server is running on http://localhost:8080
```

## API Reference

The server exposes two GET endpoints that return plain-text responses. All responses use HTTP status code 200 (OK).

### Endpoints

| Method | Path       | Response         | Content Type | Status Code |
|--------|------------|------------------|--------------|-------------|
| GET    | `/`        | `Hello world`    | text/html    | 200         |
| GET    | `/evening` | `Good evening`   | text/html    | 200         |

### Examples

#### GET / — Root Endpoint

Returns a plain-text greeting.

**Request:**

```bash
curl http://localhost:3000/
```

**Response:**

```
Hello world
```

#### GET /evening — Evening Greeting Endpoint

Returns a plain-text evening greeting.

**Request:**

```bash
curl http://localhost:3000/evening
```

**Response:**

```
Good evening
```

## Project Structure

The project follows a flat, single-file architecture for tutorial simplicity. All files reside in the project root directory:

```
express-tutorial-server/
├── .gitignore          # Version control exclusion patterns
├── index.js            # Express.js server entry point
├── package.json        # Node.js project manifest
├── package-lock.json   # Dependency lock file (auto-generated)
└── README.md           # Project documentation (this file)
```

### File Descriptions

| File                | Description                                                                 |
|---------------------|-----------------------------------------------------------------------------|
| `index.js`          | Express.js server entry point — contains route handlers and server binding  |
| `package.json`      | Node.js project manifest — declares dependencies, scripts, and metadata     |
| `package-lock.json` | Dependency lock file — locks exact package versions for reproducible builds  |
| `.gitignore`        | Version control exclusion patterns — prevents tracking of generated files    |
| `README.md`         | Project documentation — installation, usage, and API reference (this file)  |

## Technology Stack

- **Runtime:** [Node.js](https://nodejs.org/) >= 18.0.0
- **Framework:** [Express.js](https://expressjs.com/) 5.2.1
- **Module System:** CommonJS (`require()` / `module.exports`)

## License

This project is licensed under the [ISC License](https://opensource.org/licenses/ISC).
