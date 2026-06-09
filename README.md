# Turism Fullstack

Proyecto integrado con Spring Boot, frontend estático servido desde `src/main/resources/static` y PostgreSQL.

## Portales

```text
Portal de viajes:       http://localhost:8080/extranet/
Panel administrativo:   http://localhost:8080/intranet/
```

## Credenciales demo

```text
Administrador:
Email:    admin@turism.local
Password: Admin12345

Viajero demo:
Email:    viajero@turism.local
Password: Viajero123
```

## Datos iniciales

Al levantar la aplicación se crean automáticamente:

- roles base;
- estados de reserva;
- categorías e intereses;
- administrador demo;
- viajero demo;
- operador turístico demo;
- 20 experiencias publicadas con fotos, fechas y cupos.

## Regla de negocio aplicada

- El portal de viajes es solo para viajeros.
- Los anfitriones se registran desde el panel administrativo.
- Las experiencias se cargan desde el panel administrativo usando el ID de anfitrión.
- Una experiencia nueva queda pendiente hasta que administración la apruebe.
- El cliente solo ve experiencias publicadas.

## Docker

```bash
cd docker
docker compose up --build
```

Para recrear todo desde cero, incluyendo la base de datos:

```bash
cd docker
docker compose down -v
docker compose up --build
```
