# SimpleCRM
A very rudimentary CRM web application. Add, search, and delete person records through a browser-based frontend.

Running locally on Docker, served on Tomcat/Nginx, implemented using Java and mySQL


## Stack

| Layer    | Technology                          |
|----------|-------------------------------------|
| Frontend | HTML / CSS / Vanilla JS             |
| Reverse proxy | Nginx 1.27                   |
| Backend  | Java 21 · Jakarta Servlet · Tomcat 11 |
| Database | MySQL 8.4                           |
| Build    | Maven 3.9 · Docker Compose          |
| CI/CD    | Jenkins                             |

## Project Structure

```
├── frontend/          # HTML, CSS, JS (served by Nginx)
├── backend/           # Java servlet source + Dockerfile
│   └── src/
├── database/
│   └── schema.sql     # Auto-applied on first DB start
├── nginx/
│   └── nginx.conf     # Reverse proxy config
├── docker-compose.yml
└── Jenkinsfile
```

# Prerequesites

This projects requires the latest version of **Docker** and **Docker Compose** Plugin

# Setting Up SimpleCRM

After installing **Docker** and **Docker Compose**, execute the following command:

```bash
docker compose up --build -d
```

The app is available at `http://localhost` or `127.0.0.1`

# Re-Deploying SimpleCRM

To re-deploy, execute the following command:

```bash
docker compose down
docker compose up --build -d
```

## API Endpoints

| Method | Path         | Description           |
|--------|--------------|-----------------------|
| POST   | /api/person  | Add a person          |
| GET    | /api/search?q= | Search persons      |
| POST   | /api/delete  | Delete a person by ID |

## CI/CD

Deploys happens via Compose, and a health check is performed against all three services (nginx, tomcat, mysql).

## Database

The `person` table is created automatically from `database/schema.sql` on first container start. Data is persisted in a named Docker volume (`mysql-data`).

A unique constraint prevents duplicate entries with the same first name, last name, and street.
