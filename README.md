# Gestión de Proyectos Académicos – API (Spring Boot + PostgreSQL)

API para administrar **Estudiantes**, **Tutores** y **Proyectos** con seguridad **JWT** y reglas de negocio claras:

- **Tutor (1–N) Proyecto**
- **Estudiante (1–1) Proyecto**
- **ÚNICO ADMIN** en todo el sistema
- Solo **ADMIN** crea/edita/borra **Tipos de Usuario** (`TUTOR`/`ESTUDIANTE`). Por API **no** se puede crear/editar `ADMIN`.
- Email de **TUTOR** debe terminar en `@tutor.com`
- Passwords **encriptados** (BCrypt) y **write-only** en JSON

---

## ⚙️ Tech stack

- Java 17, Spring Boot 3.5.x  
- Spring Security + JWT (JJWT)  
- Spring Data JPA (Hibernate)  
- PostgreSQL (pgAdmin 4)  
- Jakarta Validation, Lombok  
- Swagger/OpenAPI (opcional)

---

## 🚀 Arranque rápido

### 1) Configurar PostgreSQL

Crea una base y un usuario, luego ajusta `src/main/resources/application.properties`:

```properties
server.port=9090

spring.datasource.url=jdbc:postgresql://localhost:5432/interciclo
spring.datasource.username=postgres
spring.datasource.password=postgres

spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=false
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true
```

> **Prod tip:** mueve el secreto JWT a variables/propiedades externas (en dev está en código).

### 2) Ejecutar

```bash
mvn spring-boot:run
# o
mvn clean package && java -jar target/interciclo-0.0.1-SNAPSHOT.jar
```

---

## 🔒 Seguridad y roles

- **Público (sin token):**
  - `POST /api/auth/register-admin` → crea el **único ADMIN** (y el `TipoUsuario ADMIN` si no existe).
  - `POST /api/auth/login` → entrega JWT.
- **Protegido (requiere `Authorization: Bearer <JWT>`):**
  - Todo lo demás.

**Permisos por rol:**

<!-- HTML table to preserve layout on GitHub -->
<table>
  <thead>
    <tr>
      <th>Recurso / Método</th>
      <th>Público</th>
      <th>TUTOR</th>
      <th>ADMIN</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><strong>POST</strong> <code>/api/auth/register-admin</code></td>
      <td style="text-align:center;">✅</td>
      <td style="text-align:center;">n/a</td>
      <td style="text-align:center;">n/a</td>
    </tr>
    <tr>
      <td><strong>POST</strong> <code>/api/auth/login</code></td>
      <td style="text-align:center;">✅</td>
      <td style="text-align:center;">n/a</td>
      <td style="text-align:center;">n/a</td>
    </tr>
    <tr>
      <td><strong>GET</strong> <code>/api/tipos-usuario</code> / <code>{id}</code></td>
      <td style="text-align:center;">❌</td>
      <td style="text-align:center;">✅</td>
      <td style="text-align:center;">✅</td>
    </tr>
    <tr>
      <td><strong>POST/PUT/PATCH/DELETE</strong> <code>/api/tipos-usuario</code><br/><em>(Prohibido crear/editar nombre ADMIN)</em></td>
      <td style="text-align:center;">❌</td>
      <td style="text-align:center;">❌</td>
      <td style="text-align:center;">✅</td>
    </tr>
    <tr>
      <td><strong>GET</strong> <code>/api/tutores</code> / <code>{id}</code></td>
      <td style="text-align:center;">❌</td>
      <td style="text-align:center;">✅</td>
      <td style="text-align:center;">✅</td>
    </tr>
    <tr>
      <td><strong>POST/PUT/PATCH/DELETE</strong> <code>/api/tutores</code><br/><em>(Recomendado: solo ADMIN)</em></td>
      <td style="text-align:center;">❌</td>
      <td style="text-align:center;">❌</td>
      <td style="text-align:center;">✅</td>
    </tr>
    <tr>
      <td><strong>GET</strong> <code>/api/estudiantes</code> / <code>{id}</code></td>
      <td style="text-align:center;">❌</td>
      <td style="text-align:center;">✅</td>
      <td style="text-align:center;">✅</td>
    </tr>
    <tr>
      <td><strong>POST/PUT/PATCH/DELETE</strong> <code>/api/estudiantes</code></td>
      <td style="text-align:center;">❌</td>
      <td style="text-align:center;">✅</td>
      <td style="text-align:center;">✅</td>
    </tr>
    <tr>
      <td><strong>GET</strong> <code>/api/proyectos</code> / <code>{id}</code></td>
      <td style="text-align:center;">❌</td>
      <td style="text-align:center;">✅</td>
      <td style="text-align:center;">✅</td>
    </tr>
    <tr>
      <td><strong>POST/PUT/PATCH/DELETE</strong> <code>/api/proyectos</code></td>
      <td style="text-align:center;">❌</td>
      <td style="text-align:center;">✅</td>
      <td style="text-align:center;">✅</td>
    </tr>
  </tbody>
</table>

---

## 🧱 Modelo de datos (resumen)

- **TipoUsuario**: <code>id</code>, <code>nombre</code> (único), <code>descripcion</code>  
  - Por API: crear/editar solo <code>TUTOR</code> y <code>ESTUDIANTE</code>. <code>ADMIN</code> solo se crea en <code>/api/auth/register-admin</code>.
- **Tutor**: <code>id</code>, <code>nombre</code>, <code>apellido</code>, <code>email</code> (único), <code>username</code> (único), <code>password</code> (write-only), <code>estaActivo</code>, <code>tituloAcademico</code>, <code>departamento</code>, <code>tipoUsuario_id</code>  
  - Si rol = <code>TUTOR</code>, el email debe terminar en <code>@tutor.com</code>.
- **Estudiante**: <code>id</code>, <code>nombre</code>, <code>apellido</code>, <code>email</code> (único), <code>username</code> (único), <code>password</code> (write-only), <code>estaActivo</code>, <code>codigo</code> (único), <code>carrera</code>, <code>ciclo</code>, <code>tipoUsuario_id</code>
- **Proyecto**: <code>id</code>, <code>codigo</code> (único), <code>titulo</code>, <code>resumen</code>, <code>objetivos</code>, <code>areaTematica</code>, <code>palabrasClave</code>, <code>fechaInicio</code>, <code>fechaFin</code>, <code>estado</code> (enum), <code>calificacionFinal</code>, <code>urlRepositorio</code>, <code>urlDocumento</code>, <code>tutor_id</code>, <code>estudiante_id</code> (único)  
  - UNIQUE en <code>estudiante_id</code> ⇒ 1–1 Estudiante–Proyecto.

---

## 🔑 Autenticación (ejemplos)

### 1) Crear ADMIN (una sola vez, sin token)

```bash
curl -X POST http://localhost:9090/api/auth/register-admin  -H "Content-Type: application/json"  -d '{
  "email": "admin@acceso.com",
  "username": "ADMIN",
  "password": "admin1234",
  "nombre": "ADMIN",
  "apellido": "ADMIN"
 }'
```

- 201 Created (si no existe)
- 409 Conflict (si ya hay un ADMIN)

### 2) Login (obtener JWT)

```bash
curl -X POST http://localhost:9090/api/auth/login  -H "Content-Type: application/json"  -d '{
  "username": "ADMIN",
  "password": "admin1234"
 }'
```

Respuesta:
```json
{
  "accessToken": "<JWT>",
  "tokenType": "Bearer",
  "username": "ADMIN",
  "role": "ADMIN"
}
```

Guarda el token:
```bash
TOKEN=<JWT>
```

---

## 📚 Endpoints (con ejemplos)

> Todos los JSON son **válidos** (sin comentarios) y con headers:
>
> ```text
> Authorization: Bearer <JWT>
> Content-Type: application/json
> ```

### A) Tipos de Usuario

**Crear TUTOR** (solo ADMIN):
```bash
curl -X POST http://localhost:9090/api/tipos-usuario  -H "Authorization: Bearer $TOKEN"  -H "Content-Type: application/json"  -d '{
  "nombre": "TUTOR",
  "descripcion": "Usuario tutor del sistema"
 }'
```

**Crear ESTUDIANTE** (solo ADMIN):
```bash
curl -X POST http://localhost:9090/api/tipos-usuario  -H "Authorization: Bearer $TOKEN"  -H "Content-Type: application/json"  -d '{
  "nombre": "ESTUDIANTE",
  "descripcion": "Usuario estudiante"
 }'
```

**Listar / Obtener:**
```bash
curl -H "Authorization: Bearer $TOKEN" http://localhost:9090/api/tipos-usuario
curl -H "Authorization: Bearer $TOKEN" http://localhost:9090/api/tipos-usuario/1
```

**Actualizar (PUT) / Parcial (PATCH) / Eliminar (solo ADMIN):**
```bash
curl -X PUT http://localhost:9090/api/tipos-usuario/2  -H "Authorization: Bearer $TOKEN"  -H "Content-Type: application/json"  -d '{
  "nombre": "TUTOR",
  "descripcion": "Tutor académico"
 }'

curl -X PATCH http://localhost:9090/api/tipos-usuario/2  -H "Authorization: Bearer $TOKEN"  -H "Content-Type: application/json"  -d '{
  "descripcion": "Tutor académico (actualizado)"
 }'

curl -X DELETE http://localhost:9090/api/tipos-usuario/2  -H "Authorization: Bearer $TOKEN"
```

---

### B) Tutores

> **Crear/editar/eliminar = solo ADMIN** (recomendado).  
> El servicio asigna **TUTOR** por defecto si no envías `tipoUsuario`.  
> Si rol = TUTOR → `email` debe terminar en `@tutor.com`.

**Crear (sin `tipoUsuario`):**
```bash
curl -X POST http://localhost:9090/api/tutores  -H "Authorization: Bearer $TOKEN"  -H "Content-Type: application/json"  -d '{
  "nombre": "Matías",
  "apellido": "Tutor",
  "email": "matias@tutor.com",
  "username": "mtutor",
  "password": "Secreta123",
  "estaActivo": true,
  "tituloAcademico": "Ing.",
  "departamento": "Sistemas"
 }'
```

**Listar / Obtener:**
```bash
curl -H "Authorization: Bearer $TOKEN" http://localhost:9090/api/tutores
curl -H "Authorization: Bearer $TOKEN" http://localhost:9090/api/tutores/1
```

**Actualizar (PUT):**
```bash
curl -X PUT http://localhost:9090/api/tutores/1  -H "Authorization: Bearer $TOKEN"  -H "Content-Type: application/json"  -d '{
  "nombre": "Matías",
  "apellido": "Tutor",
  "email": "matias@tutor.com",
  "username": "mtutor",
  "password": "NuevaClave123",
  "estaActivo": true,
  "tituloAcademico": "MSc.",
  "departamento": "Ciencias"
 }'
```

**Parcial (PATCH):**
```bash
curl -X PATCH http://localhost:9090/api/tutores/1  -H "Authorization: Bearer $TOKEN"  -H "Content-Type: application/json"  -d '{
  "departamento": "Computación",
  "estaActivo": true
 }'
```

**Eliminar:**
```bash
curl -X DELETE http://localhost:9090/api/tutores/1  -H "Authorization: Bearer $TOKEN"
```

---

### C) Estudiantes

> **ADMIN/TUTOR** pueden gestionar estudiantes.  
> Unicidades: `email`, `username`, `codigo`.

**Crear:**
```bash
curl -X POST http://localhost:9090/api/estudiantes  -H "Authorization: Bearer $TOKEN"  -H "Content-Type: application/json"  -d '{
  "nombre": "Ana",
  "apellido": "Pérez",
  "email": "ana@demo.com",
  "username": "aperez",
  "password": "Clave123",
  "estaActivo": true,
  "codigo": "A001",
  "carrera": "Sistemas",
  "ciclo": "VI"
 }'
```

**Listar / Obtener:**
```bash
curl -H "Authorization: Bearer $TOKEN" http://localhost:9090/api/estudiantes
curl -H "Authorization: Bearer $TOKEN" http://localhost:9090/api/estudiantes/1
```

**Actualizar / Parcial / Eliminar:**
```bash
curl -X PUT http://localhost:9090/api/estudiantes/1  -H "Authorization: Bearer $TOKEN"  -H "Content-Type: application/json"  -d '{
  "nombre": "Ana",
  "apellido": "Pérez",
  "email": "ana@demo.com",
  "username": "aperez",
  "password": "NuevaClave789",
  "estaActivo": true,
  "codigo": "A001",
  "carrera": "Software",
  "ciclo": "VII"
 }'

curl -X PATCH http://localhost:9090/api/estudiantes/1  -H "Authorization: Bearer $TOKEN"  -H "Content-Type: application/json"  -d '{
  "carrera": "Computación",
  "estaActivo": false
 }'

curl -X DELETE http://localhost:9090/api/estudiantes/1  -H "Authorization: Bearer $TOKEN"
```

---

### D) Proyectos

**Reglas:**
- `codigo` único  
- `estudiante_id` único (máximo un proyecto por estudiante)  
- `tutor` y `estudiante` deben existir  
- `estado`: `PROPUESTO`, `EN_REVISION`, `APROBADO`, `EN_DESARROLLO`, `FINALIZADO`, `RECHAZADO`  
- `calificacionFinal`: 0–100; fechas ISO `YYYY-MM-DD`

**Crear:**
```bash
curl -X POST http://localhost:9090/api/proyectos  -H "Authorization: Bearer $TOKEN"  -H "Content-Type: application/json"  -d '{
  "codigo": "PRJ-2025-001",
  "titulo": "Sistema de Gestión Académica",
  "resumen": "Proyecto de grado sobre gestión de PA",
  "objetivos": "Automatizar procesos",
  "areaTematica": "Sistemas",
  "palabrasClave": "gestión, académico, api",
  "fechaInicio": "2025-10-17",
  "fechaFin": "2026-02-15",
  "estado": "PROPUESTO",
  "calificacionFinal": null,
  "urlRepositorio": "https://github.com/org/repo",
  "urlDocumento": "https://drive.google.com/...",
  "tutor": { "id": 1 },
  "estudiante": { "id": 1 }
 }'
```

**Listar / Obtener:**
```bash
curl -H "Authorization: Bearer $TOKEN" http://localhost:9090/api/proyectos
curl -H "Authorization: Bearer $TOKEN" http://localhost:9090/api/proyectos/1
```

**Actualizar / Parcial / Eliminar:**
```bash
curl -X PUT http://localhost:9090/api/proyectos/1  -H "Authorization: Bearer $TOKEN"  -H "Content-Type: application/json"  -d '{
  "codigo": "PRJ-2025-001",
  "titulo": "Sistema v2",
  "resumen": "Resumen actualizado",
  "objetivos": "Objetivos actualizados",
  "areaTematica": "Software",
  "palabrasClave": "api, spring, postgres",
  "fechaInicio": "2025-10-17",
  "fechaFin": "2026-03-01",
  "estado": "EN_DESARROLLO",
  "calificacionFinal": 95.5,
  "urlRepositorio": "https://github.com/org/repo",
  "urlDocumento": "https://drive.google.com/...",
  "tutor": { "id": 1 },
  "estudiante": { "id": 1 }
 }'

curl -X PATCH http://localhost:9090/api/proyectos/1  -H "Authorization: Bearer $TOKEN"  -H "Content-Type: application/json"  -d '{
  "estado": "APROBADO",
  "calificacionFinal": 92.5,
  "fechaFin": "2026-02-20"
 }'

curl -X DELETE http://localhost:9090/api/proyectos/1  -H "Authorization: Bearer $TOKEN"
```

---

## ✅ Validaciones importantes

- **Único ADMIN** en `Tutor` (crear 2.º → `409 Conflict`)
- `Tutor.email` con rol TUTOR ⇒ **debe** terminar en `@tutor.com`
- Unicidades:
  - `Tutor.email`, `Tutor.username`
  - `Estudiante.email`, `Estudiante.username`, `Estudiante.codigo`
  - `Proyecto.codigo`, `Proyecto.estudiante_id` (1–1)
- **Password obligatorio** en altas; **write-only** en JSON (no se devuelve)

---

## ❗ Respuestas de error comunes

- **400 Bad Request** → JSON inválido, validaciones (estado, dominio email, rol inválido, relaciones inexistentes)
- **401 Unauthorized** → sin token o token inválido
- **403 Forbidden** → sin permisos (p. ej., crear tipo de usuario sin ser ADMIN; intentar crear/editar `ADMIN` por API)
- **404 Not Found** → id no encontrado
- **409 Conflict** → duplicados (`email`, `username`, `codigo`, 2.º ADMIN, 2.º proyecto para mismo estudiante)

---

## 🧪 Tips

- Passwords no se devuelven en respuestas (`@JsonProperty(WRITE_ONLY)`).
- CORS: `http://localhost:9090` configurado; ajusta en `CorsConfig` si tu frontend usa otro origen.
- Swagger (si lo activaste):
  - `/swagger-ui/index.html`
  - `/v3/api-docs`

---

## 📄 Licencia

Uso académico/demostrativo. Ajusta a tu preferencia.
