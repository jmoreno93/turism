# Ejecución con Docker Compose

Este proyecto sirve el backend Spring Boot y el frontend estático desde el mismo contenedor `app`.
PostgreSQL se levanta en un contenedor independiente.

## Levantar todo

Desde la raíz del proyecto:

```bash
docker compose --env-file docker/.env -f docker/docker-compose.yml up --build
```

O entrando a la carpeta Docker:

```bash
cd docker
docker compose up --build
```

## URLs

```text
Home:      http://localhost:8080/
Portal de viajes:       http://localhost:8080/extranet/
Panel administrativo:   http://localhost:8080/intranet/
```

## Credenciales iniciales y demo

```text
Administrador:
Email:    admin@turism.local
Password: Admin12345

Viajero demo:
Email:    viajero@turism.local
Password: Viajero123
```

## Base de datos

```text
Host local: localhost
Puerto:     5432
Database:   turism
Usuario:    postgres
Password:   postgres
```

Dentro de Docker, la aplicación usa:

```text
jdbc:postgresql://postgres:5432/turism
```

## pgAdmin opcional

Para levantar también pgAdmin:

```bash
cd docker
docker compose --profile tools up --build
```

Luego abrir:

```text
http://localhost:5050
```

Credenciales pgAdmin:

```text
Email:    admin@turism.local
Password: admin123
```

Para registrar el servidor en pgAdmin:

```text
Host:     postgres
Port:     5432
Database: turism
User:     postgres
Password: postgres
```

## Apagar servicios

```bash
cd docker
docker compose down
```

## Apagar y borrar la data de PostgreSQL

```bash
cd docker
docker compose down -v
```

## Reconstruir sin caché

```bash
cd docker
docker compose build --no-cache
```

## Fix rutas frontend

Las rutas `/extranet/` e `/intranet/` están resueltas por `FrontendController`, que redirige internamente a:

- `/extranet/index.html`
- `/intranet/index.html`

Después de actualizar el proyecto, reconstruir el contenedor:

```bash
docker compose down
docker compose up --build --force-recreate
```

## Datos demo

El arranque crea 20 experiencias publicadas si la base aún no tiene ese catálogo cargado. Si vienes de una versión anterior y quieres limpiar todo, usa:

```bash
docker compose down -v
docker compose up --build
```
