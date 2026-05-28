# SimpleCRM
A very rudimentary CRM web application - Running on Docker, served on Tomcat/Nginx, implemented using Java and mySQL
SimpleCRM

# Prerequesites

This projects requires the latest version of **Docker** and **Docker Compose** Plugin
# Setting Up SimpleCRM

After installing **Docker** and **Docker Compose**, execute the following command:

```bash
docker compose up --build -d
```

# Re-Deploying SimpleCRM

To re-deploy, execute the following command:

```bash
docker compose down
docker compose up --build -d
```
