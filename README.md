# Recipe Search Engine

Find recipes from what is in your kitchen. Type the ingredients you have, the ones you want at least one of, and the ones you want to avoid; the engine returns the matching recipes.

## Try it online

The application is deployed and open to anyone, with no sign-up: <https://franck-ted-dev.github.io/recipe-search-engine/>

> The backend runs on a free hosting tier that goes to sleep after 15 minutes without traffic. If nobody has used the site recently, the page shows a "Starting the service..." banner for roughly 30 seconds while the backend wakes up, then unlocks the search form.

This is an academic project, built to practise designing and shipping a complete application: search algorithm, database, REST API, web interface, tests, containerisation and hosting. It grew out of *Simple Search Engine* (a program that searches for words in a text file), a JetBrains Academy (Hyperskill) project that I implemented myself and then extended into a complete application on real data.

## What it does

Each search has three optional lists of ingredients:

| List | Meaning |
| --- | --- |
| **Must contain all of these** | Every returned recipe contains every ingredient in this list. |
| **Must contain at least one of these** | Every returned recipe contains at least one of them. |
| **Must not contain any of these** | Recipes containing any of them are left out. |

Ingredients are chosen from a fixed list of canonical names (for example `tomatoes`), so "cherry tomatoes" and "tinned tomatoes" in a recipe are both found by searching `tomatoes`. Results appear as cards; each card opens a dialog with the country of origin, the ingredient quantities, the instructions and a video link.

The recipe data comes from the public API of [TheMealDB](https://www.themealdb.com/), which the application queries only to fill an empty database: 790 meals, each with a name, free-text ingredient names and quantities, instructions, an image and a video link. I cleaned and restructured this raw data for the application (canonical ingredients, matching, the three-list search) and stored the result in my own PostgreSQL database. The application then runs entirely on that database and does not call TheMealDB again; only the recipe images are still loaded from the URLs it provided. Everything that turns the raw data into a searchable product is built in this project: the canonical ingredient layer, the matching, the inverted-index search with its three kinds of list, the REST API, the web interface, the tests and the deployment.

The exclusion list helps with allergies and dislikes, but it is not a safety guarantee: it only sees what is in the source data, so always check a recipe's full ingredient list.

## Architecture

```
 Browser (GitHub Pages)
 HTML / CSS / vanilla JS
        |  fetch (JSON, CORS)
        v
 Java 21 backend (Render, Docker)
 com.sun.net.httpserver.HttpServer
        |  JDBC
        v
 PostgreSQL (Neon)
```

| Part | Technology |
| --- | --- |
| Backend | Java 21, the JDK's built-in `HttpServer` (no framework), `org.json` |
| Database | PostgreSQL 16 locally (Docker Compose), Neon in production; plain JDBC, no ORM |
| Frontend | HTML, CSS and JavaScript with no framework or build step |
| Tests | JUnit 5, Testcontainers (real PostgreSQL), WireMock (mocked TheMealDB) |
| Packaging | Maven, multi-stage `Dockerfile` |
| Hosting | Render (backend), Neon (database), GitHub Pages published by a GitHub Actions workflow (frontend) |

### API

| Method and path | Purpose |
| --- | --- |
| `GET /health` | Readiness check (used by the hosting platform and by the frontend at startup). |
| `GET /canonicalingredients` | The list of ingredient names the user can choose from: `{"ingredients": [...]}`. |
| `POST /search` | Body `{"all": [...], "any": [...], "none": [...]}`; returns `{"recipes": [...]}`. |
| `GET /recipes/{id}` | One recipe by id (`404` if unknown). |

### Key design decisions

- **Inverted index + Strategy pattern.** The search engine maps each ingredient to the set of recipes containing it. The three list types are three interchangeable strategies (`AllStrategy`, `AnyStrategy`, `NoneStrategy`) working on those sets, which keeps the engine independent of the matching rule. The strategies copy the sets before changing them, so the index itself is never modified.
- **Canonical ingredients.** The ingredient names in the source data are free text (more than 1,200 distinct raw names), so I built a curated list of canonical names and match it against each raw name with a "contains" rule (the longest canonical name wins when several match). The search runs on the canonical names. After import, all 8,146 ingredient rows were linked to a canonical ingredient. Internally the engine uses richer model classes carrying the canonical name, while the API exposes the original recipe data.
- **An own database, filled once.** The source data is fetched only to fill an empty database, and then lives in PostgreSQL. Importing takes several minutes over a remote connection, so each startup step checks whether its data is already present, and the production database was filled once from a local machine: a hosted restart takes seconds instead of minutes.
- **Native HTTP server with composable filters.** Cross-cutting concerns (CORS, allowed HTTP method per route, unknown routes) are small `Filter` and `HttpHandler` classes attached to each route.
- **Configuration through environment variables**, with local defaults and a warning in the logs whenever a default is used.
- **Input checked in the browser.** The search form only accepts names from the canonical list, flags unknown ingredients next to the field and refuses an empty search before sending any request.

## Project structure

```
src/main/java/com/github/franckteddev/search/
    Main.java          startup sequence and HTTP routes
    connexion/         calls to the TheMealDB API
    mapper/            raw JSON into Recipe objects
    fetchandsave/      the one-time import pipelines
    db/                JDBC repositories
    search/            inverted index and the three search strategies
    handler/, filter/  HTTP handlers, CORS and HTTP-method filters
    model/, utility/   data classes and helpers
src/main/resources/ingredients_canonical.txt    the canonical ingredient list
src/test/java/...                                tests
frontend/                                        the web page (index.html, style.css, script.js)
schema.sql, docker-compose.yaml, Dockerfile      database schema and containers
.github/workflows/                               frontend deployment to GitHub Pages
```

## Tests

- Unit tests for the three search strategies and the inverted-index search engine.
- Integration tests against a real PostgreSQL started by Testcontainers (repositories and the import pipelines), with TheMealDB replaced by WireMock.

Run them with Maven; Docker must be running, because Testcontainers starts the database:

```
mvn test
```

## Run it locally

You need Git, Java 21, Maven, Docker (with `docker compose`) and Python 3 (to serve the web page). Ports 5432, 8000 and 5500 must be free. The commands are for Linux and macOS.

The application is three programs running side by side: the database, the backend and a small web server for the page. The last two each keep a terminal busy, so use two terminals.

1. Get the code:

   ```
   git clone https://github.com/franck-ted-dev/recipe-search-engine.git
   cd recipe-search-engine
   ```

2. Start PostgreSQL and create the tables (once). Wait a few seconds between the two commands; if the database says it is starting up, run the second one again.

   ```
   docker compose up -d
   docker exec -i recipe-search-db psql -U recipe_user -d recipe_search < schema.sql
   ```

   To start again from an empty database, run `docker compose down -v` and repeat this step.

3. Build the backend:

   ```
   mvn package -DskipTests
   ```

4. Start the backend (first terminal):

   ```
   CORS_ORIGIN=http://localhost:5500 java -cp "target/simple-search-engine-1.0.0.jar:target/lib/*" com.github.franckteddev.search.Main
   ```

   On the first start, it loads the canonical ingredients and imports the recipes from TheMealDB, which can take several minutes; later starts take seconds. The backend is ready when `curl http://localhost:8000/health` answers `{"status":"OK"}`. The `... is not set, using default value.` lines in the log are expected locally.

   `CORS_ORIGIN` must be the exact address of the web page served in the next step. Its default is `http://localhost:63342`, the address of IntelliJ's built-in web server.

5. Serve the web page (second terminal). The port must match `CORS_ORIGIN`:

   ```
   python3 -m http.server 5500 --directory frontend
   ```

6. Open <http://localhost:5500>.

Common issues:

- **`Address already in use` when starting the backend:** port 8000 is taken, usually by a backend that is already running (`ss -ltnp | grep 8000` shows which program).
- **The page stays on its "Starting the service..." banner, or the search fails with a CORS error in the browser console:** `CORS_ORIGIN` does not match the address of the page, port included. Restart the backend with the right value.

### Configuration

| Variable | Default | Purpose |
| --- | --- | --- |
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/recipe_search` | JDBC connection string. |
| `DATABASE_USER` | `recipe_user` | Database user. |
| `DATABASE_PASSWORD` | `recipe_password` | Database password. |
| `PORT` | `8000` | Port the HTTP server listens on (set by the hosting platform in production). Served from `localhost`, the page expects 8000. |
| `CORS_ORIGIN` | `http://localhost:63342` | Address of the web page allowed to read the API's answers. |

The defaults match `docker-compose.yaml`, so locally only `CORS_ORIGIN` needs to be set.

## Deployment

- **Backend:** a Docker image built from the `Dockerfile`, hosted on Render.
- **Database:** PostgreSQL on Neon.
- **Frontend:** published to GitHub Pages by a GitHub Actions workflow (`.github/workflows/deploy-frontend.yml`).

## Possible improvements

- A "Searching..." state and a result count on the page.
- A custom suggestion list instead of the browser's native one (mobile browsers present it differently; on iPhone it shows above the keyboard).
- Accessibility work: labels linked to the dynamically added fields, and a higher-contrast colour for the primary button.
