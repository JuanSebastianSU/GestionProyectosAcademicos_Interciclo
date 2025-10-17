Gestión de Proyectos Académicos – API (Spring Boot + PostgreSQL)

API para administrar Estudiantes, Tutores y Proyectos con seguridad JWT y reglas de negocio claras:

Tutor (1–N) Proyecto

Estudiante (1–1) Proyecto

ÚNICO ADMIN en todo el sistema

Solo ADMIN crea/edita/borra Tipos de Usuario (TUTOR/ESTUDIANTE). Por API no se puede crear ADMIN.

Email de TUTOR debe terminar en @tutor.com

Passwords encriptados (BCrypt) y write-only en JSON

⚙️ Tech stack

Java 17, Spring Boot 3.5.x

Spring Security + JWT (JJWT)

Spring Data JPA (Hibernate)

PostgreSQL (pgAdmin 4)

Jakarta Validation, Lombok

Swagger/OpenAPI (opcional)

🚀 Arranque rápido
1) Configurar PostgreSQL

Crea una base y un usuario, luego ajusta src/main/resources/application.properties:

server.port=9090

spring.datasource.url=jdbc:postgresql://localhost:5432/interciclo
spring.datasource.username=postgres
spring.datasource.password=postgres

spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=false
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true


Para producción, mueve el secreto JWT a variables/propiedades externas (ahora está en código para dev).

2) Ejecutar
mvn spring-boot:run
# o
mvn clean package && java -jar target/interciclo-0.0.1-SNAPSHOT.jar

🔒 Seguridad y roles

Público (sin token):

POST /api/auth/register-admin → crea el único ADMIN (y el TipoUsuario ADMIN si no existe).

POST /api/auth/login → entrega JWT.

Protegido (requiere Authorization: Bearer <JWT>):

Todo lo demás.

Permisos:

ADMIN: CRUD de Tipos de Usuario (solo TUTOR/ESTUDIANTE), CRUD de Tutores, Estudiantes y Proyectos.

TUTOR: CRUD de Estudiantes y Proyectos (según tu configuración), lectura de Tutores.

ADMIN es único (el servicio lo valida).

🧱 Modelo de datos (resumen)

TipoUsuario (id, nombre[unique], descripcion)

Permitidos por API: TUTOR, ESTUDIANTE (ADMIN solo por /register-admin)

Tutor (id, nombre, apellido, email[unique], username[unique], password, estaActivo, tituloAcademico, departamento, tipoUsuario_id)

Email @tutor.com si rol = TUTOR

Estudiante (id, nombre, apellido, email[unique], username[unique], password, estaActivo, codigo[unique], carrera, ciclo, tipoUsuario_id)

Proyecto (id, codigo[unique], título, resumen, …, estado enum, tutor_id, estudiante_id[unique])

FK a tutor y estudiante

UNIQUE en estudiante_id ⇒ 1–1 Estudiante–Proyecto

🔑 Autenticación
1) Crear ADMIN (una sola vez, sin token)
curl -X POST http://localhost:9090/api/auth/register-admin \
 -H "Content-Type: application/json" \
 -d '{
   "email": "admin@acceso.com",
   "username": "ADMIN",
   "password": "admin1234",
   "nombre": "ADMIN",
   "apellido": "ADMIN"
 }'
# 201 Created (si no existe); 409 Conflict si ya hay un ADMIN

2) Login (obtener JWT)
curl -X POST http://localhost:9090/api/auth/login \
 -H "Content-Type: application/json" \
 -d '{"username":"ADMIN","password":"admin1234"}'
# Respuesta:
# {
#   "accessToken":"<JWT>",
#   "tokenType":"Bearer",
#   "username":"ADMIN",
#   "role":"ADMIN"
# }


Guarda el token en TOKEN=<JWT> para siguientes llamadas.


🛂 Permisos y restricciones por rol (muy importante)
Resumen por endpoint / método
Recurso / Método	Público	TUTOR	ADMIN
/api/auth/register-admin (POST)	✅	n/a	n/a
/api/auth/login (POST)	✅	n/a	n/a
/api/tipos-usuario (GET, GET/{id})	❌	✅	✅
/api/tipos-usuario (POST, PUT, PATCH, DELETE)	❌	❌	✅ (prohibido crear/editar nombre ADMIN)
/api/tutores (GET, GET/{id})	❌	✅	✅
/api/tutores (POST, PUT, PATCH, DELETE)	❌	❌ (recomendado)	✅
/api/estudiantes (GET, GET/{id})	❌	✅	✅
/api/estudiantes (POST, PUT, PATCH, DELETE)	❌	✅	✅
/api/proyectos (GET, GET/{id})	❌	✅	✅
/api/proyectos (POST, PUT, PATCH, DELETE)	

📚 Endpoints (con ejemplos)

Todos los JSON sin comentarios y con header:

Authorization: Bearer <JWT>
Content-Type: application/json

A) Tipos de Usuario

Solo ADMIN puede crear/editar/eliminar.
Prohibido crear/editar el nombre ADMIN por aquí.

Crear TUTOR

curl -X POST http://localhost:9090/api/tipos-usuario \
 -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
 -d '{"nombre":"TUTOR","descripcion":"Usuario tutor del sistema"}'


Crear ESTUDIANTE

curl -X POST http://localhost:9090/api/tipos-usuario \
 -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
 -d '{"nombre":"ESTUDIANTE","descripcion":"Usuario estudiante"}'


Listar / Obtener

curl -H "Authorization: Bearer $TOKEN" http://localhost:9090/api/tipos-usuario
curl -H "Authorization: Bearer $TOKEN" http://localhost:9090/api/tipos-usuario/1


Actualizar (PUT) / Parcial (PATCH) / Eliminar

curl -X PUT http://localhost:9090/api/tipos-usuario/2 \
 -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
 -d '{"nombre":"TUTOR","descripcion":"Tutor académico"}'

curl -X PATCH http://localhost:9090/api/tipos-usuario/2 \
 -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
 -d '{"descripcion":"Tutor académico (actualizado)"}'

curl -X DELETE http://localhost:9090/api/tipos-usuario/2 \
 -H "Authorization: Bearer $TOKEN"

B) Tutores

Crear/editar/eliminar: por defecto lo dejamos solo ADMIN (recomendado).
El servicio asigna TUTOR por defecto si no envías tipoUsuario.
Si rol = TUTOR → email debe terminar en @tutor.com.

Crear (sin tipoUsuario en el body)

curl -X POST http://localhost:9090/api/tutores \
 -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
 -d '{
   "nombre":"Matías",
   "apellido":"Tutor",
   "email":"matias@tutor.com",
   "username":"mtutor",
   "password":"Secreta123",
   "estaActivo":true,
   "tituloAcademico":"Ing.",
   "departamento":"Sistemas"
 }'


Listar / Obtener

curl -H "Authorization: Bearer $TOKEN" http://localhost:9090/api/tutores
curl -H "Authorization: Bearer $TOKEN" http://localhost:9090/api/tutores/1


Actualizar (PUT)

curl -X PUT http://localhost:9090/api/tutores/1 \
 -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
 -d '{
   "nombre":"Matías",
   "apellido":"Tutor",
   "email":"matias@tutor.com",
   "username":"mtutor",
   "password":"NuevaClave123",
   "estaActivo":true,
   "tituloAcademico":"MSc.",
   "departamento":"Ciencias"
 }'


Parcial (PATCH)

curl -X PATCH http://localhost:9090/api/tutores/1 \
 -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
 -d '{"departamento":"Computación","estaActivo":true}'


Eliminar

curl -X DELETE http://localhost:9090/api/tutores/1 \
 -H "Authorization: Bearer $TOKEN"

C) Estudiantes

ADMIN/TUTOR pueden gestionar estudiantes.
Unicidades: email, username, codigo.

Crear

curl -X POST http://localhost:9090/api/estudiantes \
 -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
 -d '{
   "nombre":"Ana",
   "apellido":"Pérez",
   "email":"ana@demo.com",
   "username":"aperez",
   "password":"Clave123",
   "estaActivo":true,
   "codigo":"A001",
   "carrera":"Sistemas",
   "ciclo":"VI"
 }'


Listar / Obtener

curl -H "Authorization: Bearer $TOKEN" http://localhost:9090/api/estudiantes
curl -H "Authorization: Bearer $TOKEN" http://localhost:9090/api/estudiantes/1


Actualizar (PUT) / Parcial (PATCH) / Eliminar

curl -X PUT http://localhost:9090/api/estudiantes/1 \
 -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
 -d '{
   "nombre":"Ana",
   "apellido":"Pérez",
   "email":"ana@demo.com",
   "username":"aperez",
   "password":"NuevaClave789",
   "estaActivo":true,
   "codigo":"A001",
   "carrera":"Software",
   "ciclo":"VII"
 }'

curl -X PATCH http://localhost:9090/api/estudiantes/1 \
 -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
 -d '{"carrera":"Computación","estaActivo":false}'

curl -X DELETE http://localhost:9090/api/estudiantes/1 \
 -H "Authorization: Bearer $TOKEN"

D) Proyectos

Reglas:

codigo único

estudiante_id único (máximo un proyecto por estudiante)

tutor y estudiante deben existir

estado en: PROPUESTO | EN_REVISION | APROBADO | EN_DESARROLLO | FINALIZADO | RECHAZADO

Fechas ISO YYYY-MM-DD, calificacionFinal 0–100

Crear

curl -X POST http://localhost:9090/api/proyectos \
 -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
 -d '{
   "codigo":"PRJ-2025-001",
   "titulo":"Sistema de Gestión Académica",
   "resumen":"Proyecto de grado sobre gestión de PA",
   "objetivos":"Automatizar procesos",
   "areaTematica":"Sistemas",
   "palabrasClave":"gestión, académico, api",
   "fechaInicio":"2025-10-17",
   "fechaFin":"2026-02-15",
   "estado":"PROPUESTO",
   "calificacionFinal": null,
   "urlRepositorio":"https://github.com/org/repo",
   "urlDocumento":"https://drive.google.com/...",
   "tutor":{"id":1},
   "estudiante":{"id":1}
 }'


Listar / Obtener

curl -H "Authorization: Bearer $TOKEN" http://localhost:9090/api/proyectos
curl -H "Authorization: Bearer $TOKEN" http://localhost:9090/api/proyectos/1


Actualizar (PUT) / Parcial (PATCH) / Eliminar

curl -X PUT http://localhost:9090/api/proyectos/1 \
 -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
 -d '{
   "codigo":"PRJ-2025-001",
   "titulo":"Sistema v2",
   "resumen":"Resumen actualizado",
   "objetivos":"Objetivos actualizados",
   "areaTematica":"Software",
   "palabrasClave":"api, spring, postgres",
   "fechaInicio":"2025-10-17",
   "fechaFin":"2026-03-01",
   "estado":"EN_DESARROLLO",
   "calificacionFinal":95.5,
   "urlRepositorio":"https://github.com/org/repo",
   "urlDocumento":"https://drive.google.com/...",
   "tutor":{"id":1},
   "estudiante":{"id":1}
 }'

curl -X PATCH http://localhost:9090/api/proyectos/1 \
 -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
 -d '{"estado":"APROBADO","calificacionFinal":92.5,"fechaFin":"2026-02-20"}'

curl -X DELETE http://localhost:9090/api/proyectos/1 \
 -H "Authorization: Bearer $TOKEN"

✅ Validaciones importantes

Único ADMIN en Tutor (crear 2.º → 409 Conflict)

Tutor.email con rol TUTOR ⇒ debe terminar en @tutor.com

Unicidades:

Tutor.email, Tutor.username

Estudiante.email, Estudiante.username, Estudiante.codigo

Proyecto.codigo, Proyecto.estudiante_id (1–1)

Password obligatorio en altas; write-only en JSON (no se devuelve).

❗ Respuestas de error comunes

400 Bad Request → JSON inválido, validaciones (estado, dominio email, rol inválido, relaciones inexistentes)

401 Unauthorized → sin token o token inválido

403 Forbidden → sin permisos (p.ej., crear tipo de usuario sin ser ADMIN; intentar crear/editar ADMIN por API)

404 Not Found → id no encontrado

409 Conflict → duplicados (email, username, codigo, 2.º ADMIN, 2.º proyecto para mismo estudiante)