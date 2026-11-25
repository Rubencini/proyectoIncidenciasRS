# Proyecto: API de incidencias del centro con JWT

API REST para registrar y gestionar incidencias del centro (averías, problemas con equipos).
Autenticación con JWT y permisos por rol. Incluye un frontend básico en HTML y JavaScript.

## Tecnologías
- Java 21, Maven, Spring Boot 4.2.0
- Spring Web (`spring-boot-starter-webmvc`), Spring Data JPA, MySQL
- Spring Security con OAuth2 Resource Server (solo para validar el JWT)
- JJWT 0.13.0 (solo para generar y firmar el JWT)
- Lombok, Bean Validation
- Frontend: HTML, CSS y JavaScript sin librerías, servido desde `src/main/resources/static`

## Estructura de paquetes (`org.example.proyectoincidencias`)
- `entities`: entidades JPA y enums (`Usuario`, `Incidencia`, `EstadoIncidencia`, `Rol`)
- `repositories`: interfaces que extienden `JpaRepository`
- `services`: lógica de negocio (`JwtService`, `CustomUserDetailsService`, `IncidenciaService`, `UsuarioService`)
- `controllers`: endpoints REST, sin lógica de negocio
- `dto`: records para datos de entrada y salida
- `config`: configuración de seguridad (`SecurityConfig`)
- `exception`: excepciones propias y `GlobalExceptionHandler`

## Convenciones
- Código y comentarios en español. Nombres de clases en `PascalCase` y de métodos y variables en `camelCase`.
- Inyección de dependencias por constructor con `@RequiredArgsConstructor` y campos `private final`.
- Entidades con `@Getter`, `@Setter` y `@NoArgsConstructor` de Lombok. No usar `@Data` en entidades JPA.
- Los controladores nunca reciben ni devuelven entidades: usan DTO (records).
- Las respuestas de error son JSON con una clave `error` o un mapa campo-mensaje, gestionadas en `GlobalExceptionHandler`.
- La clave del JWT y su caducidad se leen de `application.properties` (`jwt.secret`, `jwt.expiration`).
- Los roles se guardan como texto en `Usuario.roles` (`ROLE_USER`, `ROLE_TECNICO`, `ROLE_ADMIN`), un solo rol por usuario.
- En el frontend se escribe en el DOM con `textContent`, nunca con `innerHTML` con datos del usuario.

## Reglas de seguridad que no se pueden romper
- El registro siempre asigna `ROLE_USER`. Ningún DTO de registro tiene campo de roles.
- El autor de una incidencia sale de `Authentication`, nunca de un dato enviado por el cliente.
- Un `ROLE_USER` solo accede a sus incidencias: la comprobación se hace en la consulta (`findByIdAndAutorEmail`), con el email del token.
- Los permisos se aplican siempre en el backend (`SecurityConfig` y `@PreAuthorize`). Ocultar botones en el frontend no cuenta como protección.
- No incluir contraseñas ni datos sensibles en el token ni en las respuestas de la API.
- No añadir en `GlobalExceptionHandler` un manejador de `Exception.class`: convertiría el 403 de `@PreAuthorize` en un 500.

## Restricciones sobre lo visto en clase
Usar solo lo visto en clase (apuntes de la unidad de autenticación y seguridad y el proyecto
`FW03_APIEmpleados_JWT`): Spring Security con `SecurityFilterChain`, `UserDetails` y `UserDetailsService`,
JWT con JJWT, `oauth2ResourceServer`, `@PreAuthorize`, `PasswordEncoder` con BCrypt,
DTO con records, `@Valid` y `GlobalExceptionHandler`.

- No añadir bibliotecas ni dependencias nuevas sin consultarlo.
- No introducir filtros propios, refresh tokens, listas negras de tokens, Spring Data REST, MapStruct ni otras técnicas no vistas.
- Si una construcción de Java o Spring no se ha visto en clase (por ejemplo `@ManyToOne`, enums con `@Enumerated`, `PATCH` o streams), explicar en una frase qué hace y cómo se usa.
- Todo el código del backend debe poder explicarse línea a línea.

## Comandos
- Ejecutar: `./mvnw spring-boot:run` o desde IntelliJ con la clase `ProyectoIncidenciasApplication`.
- Requiere MySQL en `localhost:3306` (base de datos `incidencias`, se crea sola).
- Frontend: `http://localhost:8080/`.