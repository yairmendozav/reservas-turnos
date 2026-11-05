# Sistema de reservas de turnos

API REST para crear y consultar reservas, con validaciones y protección contra solapamientos de horarios.

Proyecto de aprendizaje progresivo con Java y Spring Boot. Actualmente está implementado el servicio de reservas; Catálogo y Notificaciones se incorporarán en etapas posteriores.

## Tecnologías

- Java 25
- Spring Boot 4.1.2
- Maven y Maven Wrapper
- Spring Web MVC
- Jakarta Validation
- Spring Data JPA / Hibernate
- PostgreSQL 17
- Flyway
- Docker Compose
- JUnit y Testcontainers

## Funcionalidades actuales

- Crear reservas.
- Consultar una reserva por su UUID.
- Validar campos obligatorios, correo y fechas futuras.
- Rechazar reservas cuyo fin no sea posterior al inicio.
- Impedir reservas confirmadas solapadas para el mismo profesional.
- Devolver errores estructurados mediante ProblemDetail.
- Verificar solicitudes concurrentes con una prueba de integración.

## Organización

```text
reservas-turnos/
├── booking-service/
│   ├── src/main/java/
│   ├── src/main/resources/
│   │   ├── application.yaml
│   │   └── db/migration/
│   ├── src/test/java/
│   └── pom.xml
└── infraestructure/
    ├── compose.yaml
    └── .env.example
```

El servicio organiza sus responsabilidades en controlador, DTOs, servicio,
entidad y repositorio.

## Requisitos

- JDK 25.
- Docker Desktop iniciado, con soporte para contenedores Linux.
- Git.
- Puertos locales 8000 y 5433 disponibles.

Los comandos siguientes están escritos para PowerShell en Windows.
No es necesario instalar Maven: el proyecto incluye Maven Wrapper.

## Ejecutar localmente

### 1. Clonar el repositorio

```powershell
git clone https://github.com/yairmendozav/reservas-turnos.git
cd reservas-turnos
```

### 2. Configurar PostgreSQL

```powershell
cd infraestructure
Copy-Item .env.example .env
```

Edita `.env` y establece las credenciales para tu entorno local:

```dotenv
POSTGRES_DB=booking_db
POSTGRES_USER=booking_user
POSTGRES_PASSWORD=change_me
```

El archivo `.env` está excluido de Git.

### 3. Iniciar la base de datos

Desde `infraestructure`:

```powershell
docker compose up -d
docker compose ps
```

Espera a que `booking-db` aparezca como `healthy`.

PostgreSQL queda disponible en `localhost:5433`. Los datos se conservan
en un volumen administrado por Docker.

Las variables POSTGRES_* inicializan la base cuando el volumen está vacío.
Modificarlas después no cambia automáticamente los usuarios o bases existentes.

### 4. Ejecutar la aplicación

En la misma terminal:

```powershell
cd ..\booking-service
$env:DB_USER = "booking_user"
$env:DB_PASSWORD = "change_me"
.\mvnw.cmd spring-boot:run
```

Usa la misma contraseña que configuraste en `.env`.

La API estará disponible en:

```text
http://localhost:8000
```

También puedes ejecutar `BookingServiceApplication` desde IntelliJ,
configurando `DB_USER` y `DB_PASSWORD` en sus variables de entorno.

Flyway aplica las migraciones al arrancar. Hibernate valida que las
entidades coincidan con las tablas.

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/bookings` | Crear una reserva |
| GET | `/bookings/{id}` | Consultar una reserva |

### Crear una reserva

```http
POST http://localhost:8000/bookings
Content-Type: application/json
```

```json
{
  "customerName": "Yair",
  "customerEmail": "yair@example.com",
  "professionalId": "11111111-1111-4111-8111-111111111111",
  "serviceId": "22222222-2222-4222-8222-222222222222",
  "startsAt": "2026-12-10T15:00:00Z",
  "endsAt": "2026-12-10T15:30:00Z"
}
```

Sustituye las fechas por instantes futuros al probar el ejemplo.
La `Z` representa UTC.

Respuesta: `201 Created`, con una cabecera `Location` y el identificador:

```json
{
  "id": "uuid-generado"
}
```

### Consultar una reserva

```http
GET http://localhost:8000/bookings/{id}
```

Devuelve `200 OK` con los datos de la reserva o `404 Not Found`
si el UUID no corresponde a una reserva existente.

### Errores

| Estado | Situación |
|---|---|
| 400 | Campos inválidos o período de reserva incorrecto |
| 404 | Reserva no encontrada |
| 409 | Horario ocupado para el profesional |

## Control de concurrencia

La creación de reservas combina:

1. Un bloqueo transaccional de PostgreSQL por profesional.
2. Una consulta de reservas confirmadas solapadas.
3. Una restricción de exclusión en PostgreSQL como garantía adicional.

El bloqueo se mantiene hasta finalizar la transacción. Las creaciones para
un mismo profesional se coordinan incluso si llegan desde distintas
instancias de la aplicación.

Los intervalos incluyen el inicio y excluyen el fin. Por ello, una reserva
puede comenzar exactamente cuando termina otra.

## Prueba de concurrencia

Con Docker Desktop iniciado, ejecuta desde `booking-service`:

```powershell
.\mvnw.cmd -Dtest=BookingConcurrencyTest test
```

Testcontainers inicia un PostgreSQL temporal y separado de la base local.
Spring Boot aplica las migraciones y utiliza un puerto HTTP aleatorio.

La prueba envía dos solicitudes en paralelo para el mismo profesional
y horario, y verifica:

- Una respuesta 201.
- Una respuesta 409.
- Una sola reserva guardada.

Esta prueba cubre el flujo completo con el bloqueo por profesional.
La prueba directa de la restricción de exclusión queda pendiente.

El test generado `BookingServiceApplicationTests` carga el contexto con
la configuración local y requiere la base y las variables de entorno.
El comando anterior ejecuta únicamente la prueba aislada de concurrencia.

## Decisiones técnicas

- UUID para generar identificadores desde la aplicación.
- DTOs para definir los contratos HTTP sin exponer directamente la entidad.
- Flyway para versionar la estructura de la base de datos.
- `ddl-auto: validate` para que Hibernate compruebe el esquema.
- `open-in-view: false` para delimitar el acceso a datos.
- Instant y TIMESTAMPTZ para representar los instantes de las reservas.
- PostgreSQL real en las pruebas de integración.

## Alcance actual y próximos pasos

Actualmente existe una aplicación: `booking-service`.

Todavía no se validan los identificadores de profesionales y servicios
contra un catálogo. La API tampoco incorpora autenticación ni horarios
de atención.

Próximas funcionalidades:

- Cancelación de reservas.
- Ampliación de pruebas automatizadas.
- Horarios de atención.
- Microservicio de Catálogo.
- Autenticación y autorización.
- Notificaciones.
- Integración continua con GitHub Actions.