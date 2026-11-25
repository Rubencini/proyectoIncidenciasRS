# API de incidencias del centro con JWT

Este proyecto es la práctica de la unidad de autenticación y seguridad de Programación de Aplicaciones Utilizando Frameworks (2º DAM). Es una aplicación para registrar y gestionar incidencias del centro, por ejemplo una avería en un aula o un problema con un ordenador. La API está hecha con Spring Boot y Spring Security, usa JWT para autenticar a los usuarios y controla qué puede hacer cada uno según su rol. También tiene un frontend básico en HTML y JavaScript para probarla.

## Qué hace la aplicación

- Cualquiera puede registrarse. Al registrarse siempre se le asigna `ROLE_USER` y no puede elegir otro rol. Las contraseñas se guardan cifradas con BCrypt.
- Al hacer login, la API devuelve un JWT firmado, y ese token hay que mandarlo en las rutas protegidas.
- Cada incidencia tiene título, descripción, fecha de creación, estado (`ABIERTA`, `EN_PROCESO` o `RESUELTA`) y autor.
- Hay tres roles, con estos permisos:

| Acción | USER | TECNICO | ADMIN |
|---|---|---|---|
| Crear una incidencia | Sí | Sí | Sí |
| Consultar sus propias incidencias | Sí | Sí | Sí |
| Consultar cualquier incidencia | No | Sí | Sí |
| Cambiar el estado de una incidencia | No | Sí | Sí |
| Eliminar cualquier incidencia | No | No | Sí |
| Consultar usuarios y modificar sus roles | No | No | Sí |

- **Reto principal:** un usuario con rol `USER` solo puede ver sus incidencias, tanto en el listado como al pedir una concreta por su número. Si cambia el número de la URL no puede ver las de otros.
- El autor de una incidencia se saca siempre del token, nunca de lo que envía el cliente.

## Tecnologías

- Java 21, Maven y Spring Boot 4.2.0
- Spring Web, Spring Data JPA y MySQL
- Spring Security con OAuth2 Resource Server (para validar el JWT) y JJWT 0.13.0 (para generarlo y firmarlo)
- Lombok y Bean Validation
- Frontend con HTML, CSS y JavaScript, sin librerías, servido por el propio Spring Boot

## Requisitos

- JDK 21 o superior
- Maven (o el wrapper `mvnw` del proyecto)
- MySQL 8 en `localhost:3306`, instalado o en un contenedor Docker
- IntelliJ IDEA, que es el IDE que he usado (no es obligatorio)

## Instalación y configuración

1. Clonar el repositorio:
   ```
   git clone <URL-DEL-REPOSITORIO>
   cd proyectoIncidencias
   ```
2. Arrancar MySQL. Con Docker, por ejemplo:
   ```
   docker run --name mysql-incidencias -e MYSQL_ROOT_PASSWORD=1234 -p 3306:3306 -d mysql:8
   ```
   No hace falta crear la base de datos a mano: se crea sola al arrancar la aplicación.
3. Abrir `src/main/resources/application.properties` y poner mis datos:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/incidencias?createDatabaseIfNotExist=true
   spring.datasource.username=root
   spring.datasource.password=TU_CONTRASEÑA_DE_MYSQL

   jwt.secret=${JWT_SECRET:TU_CLAVE_BASE64}
   jwt.expiration=86400000
   ```
4. Generar una clave para firmar los tokens (32 bytes en Base64) y ponerla en lugar de `TU_CLAVE_BASE64`:
   ```
   openssl rand -base64 32
   ```
   Si existe la variable de entorno `JWT_SECRET`, se usa esa en vez de la del archivo. `jwt.expiration` está en milisegundos, así que 86400000 son 24 horas.

## Cómo ejecutarlo

**Backend:** desde IntelliJ ejecuto la clase `ProyectoIncidenciasApplication`, o desde la terminal con:

```
./mvnw spring-boot:run
```

**Frontend:** no hay que instalar nada. Los archivos están en `src/main/resources/static` y los sirve la propia aplicación, así que basta con abrir <http://localhost:8080/> en el navegador.

### Crear los usuarios para probar

1. Me registro desde el frontend (o con `POST /auth/register`) con tres usuarios: uno básico, uno que será técnico y uno que será administrador.
2. El primer administrador se asigna directamente en la base de datos, como pide el enunciado:
   ```sql
   USE incidencias;
   UPDATE usuarios SET roles = 'ROLE_ADMIN' WHERE email = 'admin@centro.es';
   ```
   Si uso Docker, antes entro en MySQL con `docker exec -it mysql-incidencias mysql -u root -p`.
3. Inicio sesión con el administrador (después del `UPDATE`, si no el token no lleva el rol nuevo) y desde el panel «Administración de usuarios» le pongo `ROLE_TECNICO` a uno de ellos.

Los roles van dentro del token, así que cuando cambio el rol de alguien tiene que cerrar sesión y volver a entrar para que le afecte.

## Endpoints principales

| Método | Ruta | Permiso | Qué hace |
|---|---|---|---|
| POST | `/auth/register` | Público | Registra un usuario con `ROLE_USER`. Devuelve 201, 409 si el email ya existe o 400 si los datos no son válidos |
| POST | `/auth/login` | Público | Devuelve `{"token": "..."}`, o 401 si las credenciales son incorrectas |
| POST | `/incidencias` | Cualquier usuario autenticado | Crea una incidencia. El autor sale del token |
| GET | `/incidencias` | Cualquier usuario autenticado | Un `USER` ve solo las suyas; `TECNICO` y `ADMIN` ven todas |
| GET | `/incidencias/{id}` | Cualquier usuario autenticado | Un `USER` solo si es suya (si no, 404); `TECNICO` y `ADMIN` cualquiera |
| PATCH | `/incidencias/{id}/estado` | `TECNICO`, `ADMIN` | Cambia el estado. Cuerpo: `{"estado":"EN_PROCESO"}` |
| DELETE | `/incidencias/{id}` | `ADMIN` | Elimina la incidencia (204) |
| GET | `/admin/usuarios` | `ADMIN` | Lista los usuarios, sin las contraseñas |
| PATCH | `/admin/usuarios/{id}/rol` | `ADMIN` | Cambia el rol. Cuerpo: `{"rol":"ROLE_TECNICO"}` |

Las rutas protegidas necesitan la cabecera `Authorization: Bearer <token>`.

Los códigos de error que salen son: **401** (sin token, token inválido o caducado), **403** (estás autenticado pero tu rol no puede hacer eso), **404** (no existe, o es una incidencia ajena para un `USER`) y **400** (datos o valores no válidos).

El token lleva `sub` (el email), `iss` (`gestion-centro-api`), `iat`, `exp`, `uid` (id del usuario), `roles` (una lista) y `nombre` como claim adicional no sensible. No lleva contraseña.

## Estructura del proyecto

```
org.example.proyectoincidencias
├── config        SecurityConfig
├── controllers   AuthController, IncidenciaController, AdminController
├── dto           records de entrada y de salida
├── entities      Usuario, Incidencia, EstadoIncidencia, Rol
├── exception     excepciones propias y GlobalExceptionHandler
├── repositories  UsuarioRepository, IncidenciaRepository
└── services      JwtService, CustomUserDetailsService, IncidenciaService, UsuarioService
```

## Herramienta de IA utilizada

- **Herramienta:** Claude, de Anthropic.
- **Tipo:** un chat (claude.ai). No es una herramienta de desarrollo ni está integrada en el IDE, así que el código lo copiaba yo a mano en IntelliJ.
- **Cómo la he usado:** le pasé el enunciado, el PDF de apuntes y el proyecto que hicimos en clase, y me fue guiando paso a paso explicándome cada archivo. Después de cada paso arrancaba la aplicación y lo probaba con Postman o con el navegador antes de seguir con el siguiente.
- **Archivo de instrucciones:** `CLAUDE.md`, con las tecnologías, las convenciones y las restricciones sobre lo que hemos visto en clase.

## Prompts que he usado

Estos son los principales mensajes que escribí, copiados tal cual, con sus faltas de ortografía. Hubo otros muy cortos, como decir que algo arrancaba bien o que había salido lo esperado, que no pongo.

**1. Planteamiento inicial** (después pegué el enunciado completo y adjunté el zip del proyecto de clase y el PDF de apuntes; el enunciado no lo copio aquí):

> Me han mandado hacer un proyecto del cual ahora te voy a pasar el enunciado y te e pasado el proyecto que hemos hecho en clase para que nos vaya sirviendo de guia para que sepas como mas o menos 
> hacer el nuevo , te voy a pasar tambien un pdf con parte de los apuntes y en este enlace : https://claude.ai/artifact/PGiBauepEtTrxwm8fi7jnC , no se si podras entrar pero prueba a hacerlo y dime si has podido o no ,
> ahi se encuentra el resto de apuntes necesarios, lee todo bien , analiza bien todo , mirate bien todo el proyecto comprimido que te e pasado y a continuacion te paso el enunciado de la practica para que me guies , y
> me vayas explicando mientras la vamos haciendo , leelo bien tambien , si tienes alguna duda preguntamela :

**2. Versiones** (Claude me había preguntado si usar Spring Boot 3.4.2 o la 4.x de la guía):

> no se cual utilizar , guiame tu

**3. Entorno:**

> uso intellij idea para que lo tengas en cuenta

**4. Dependencias:**

> esta es toda la lista de dependencias? (Spring Web, Spring Data JPA, MySQL Driver, Spring Security, OAuth2 Resource Server, Validation y Lombok)

**5. Pedir el código y las explicaciones:**

> , dame el codigo que tengo que poner en cada sitio y expplicame las cosas paso a paso

**6. Pedir ir más despacio** (Claude me había dado de golpe varias fases seguidas):

> vas muy rapido , vamos paso a paso desde el prinvipio pasando por el pom.xml , el apllication properties si es que hay q tocarlo ya y demas

**7. Paquete del proyecto:**

> se llama asi: package org.example.proyectoincidencias;

**8. Dudas para entender el código:**

> para obtener todos sus datos y poder manejarlos?

> y no usamos try catch?

**9. Emisor del token:**

> como se en jwtDecoder si tengo que poner gestion-centro-api o otro nombre?

**10. Pruebas y herramientas:**

> dime exactamente hacer esas comprobaciones en postman

> como veo las contraseñas en misql y esas cosas?

**11. Resultado que no esperaba en una prueba** (cambiar el estado con un valor inválido y con una incidencia que no existe):

~~~
en el paso 6 y 7 e puesto lo siguiente: http://localhost:8080/incidencias/9999/estado , con patch y con el rol tecnico y el siguiente cuerpo: `{"estado":"CERRADA"}` y en el caso 6 da codigo 400 :

```json
{
    "timestamp": "2026-11-22T10:07:23.160Z",
    "status": 400,
    "error": "Bad Request",
    "path": "/incidencias/2/estado"
} y en el caso 7 , tambien un 400 con la siguiente salida: {
    "timestamp": "2026-11-22T10:07:49.147Z",
    "status": 400,
    "error": "Bad Request",
    "path": "/incidencias/9999/estado"
}
```
~~~

**12. Error al ejecutarlo en otro ordenador** (después pegué el log completo de la consola):

> e cambiado de ordenador un momento , en breves vuelvo al mio pero al intenter ejecutarlo en este equipo me da estos errores y quiero saber porque :

> en este ordenador lo tengo que hacer con docker desktop , cuando vuelva a mi equipo te avisare y asi sabras que ya volvemos a usar los programas de antes pero ahora damelo para docker descktop el asignar los roles

**13. Dudas con el frontend:**

> dime exactamente donde tengo que hacer el punto 4 del paso 2

> me referia a lo que me has dado , ya e probado el paso 2 y tod ofunciona , lo unico es que si no creas una nueva incidencia no ves las que hay ya creadas al entrar pero supongo que eso lo añadiremos despues

> dime exactamente donde tengo que poner el 24b , punto 1

**14. Dudas generales sobre cómo organizar el proyecto:**

> cmo se a que excepciones tengo que hacerles una clase con extend runtime y a que le tengo q hacer dto request


## Revisión del código generado

El código me lo fue dando Claude, pero yo lo fui poniendo, adaptando y probando:

- Cambié los paquetes, el `pom.xml` (hubo que añadir Spring Data JPA, que faltaba, y usar JJWT 0.13.0 como en los apuntes) y la configuración de MySQL a mi entorno.
- La clave del JWT la generé yo y está en `application.properties`. En un entorno real solo iría en la variable de entorno `JWT_SECRET`.
- Después de cada paso lo probé con Postman o con el navegador: sin token, token inválido, token caducado, token tras reiniciar la aplicación, incidencias propias y ajenas, los permisos de cada rol y el panel de administración.

## Problemas que me han salido y decisiones que he tomado

### Problemas

- **Fallos del proyecto de clase que no copié.** Al revisarlo con Claude vimos que `Usuario.getUsername()` devolvía una cadena vacía, así que el `sub` del token salía vacío. También que la clave de firma se generaba al arrancar, por lo que los tokens dejaban de valer al reiniciar la aplicación, y que los roles iban en el token como un texto con comas, que Spring interpreta como una única autoridad. Lo resolví devolviendo el email en `getUsername()`, leyendo la clave de `application.properties` y poniendo los roles como una lista.
- **Versión de Spring Boot.** Al principio se planteó seguir el proyecto de clase (Spring Boot 3.4.2), pero mi proyecto nuevo estaba creado con la 4.2.0, que es la versión de los apuntes. Hubo que usar los nombres de starters de la 4.x y añadir a mano `spring-boot-starter-data-jpa`, que no había marcado al crear el proyecto.
- **400 en lugar de 404 al probar el cambio de estado.** Probé una incidencia que no existe con el estado `CERRADA` y me daba 400. No era un fallo: Spring convierte el JSON al enum antes de entrar en el método, así que el 404 solo sale con un estado válido. Para que el 400 sea más claro añadí un manejador de `HttpMessageNotReadableException` que explica los valores admitidos.
- **Las incidencias no aparecían al entrar.** Solo se cargaban al crear una nueva. Lo arreglé llamando a `cargarIncidencias()` desde `actualizarVista()`, que se ejecuta al iniciar sesión y al recargar la página.
- **No arrancaba en otro ordenador.** Salía `Conexión rehusada` y después `Unable to determine Dialect`, y la causa era que MySQL no estaba arrancado. Además, la base de datos no viaja con el código, así que allí tuve que volver a registrar los usuarios y asignar el rol de administrador.

### Decisiones

- **Un solo rol por usuario**, guardado como texto en `Usuario.roles`, como se hace en los apuntes. Es más sencillo de explicar y de comprobar. Si hubiera muchos roles, sería mejor una entidad propia.
- **Rol y estado como enum.** Así, el backend rechaza con un 400 cualquier valor que no sea uno de los permitidos sin tener que escribir más código. Cumple lo de validar el rol en el backend.
- **DTO en las entradas y las salidas.** El de registro no tiene campo de roles y el de crear incidencia no tiene autor, estado ni fecha, así que el cliente no puede colarlos. Las respuestas nunca llevan la contraseña.
- **Incidencia ajena: 404 y no 403.** Con un 403 se confirmaría que esa incidencia existe y alguien podría ir probando números. La comprobación está dentro de la propia consulta (`findByIdAndAutorEmail`) y usa el email del token, así que el dato ni siquiera se carga si no es del usuario.
- **Dos niveles de protección.** Las reglas generales están en `SecurityConfig` (`/auth/**` público, `/admin/**` solo `ADMIN` y el resto autenticado) y los permisos de cada operación con `@PreAuthorize`. El controlador de administración lo lleva además a nivel de clase.
- **Sin manejador de `Exception.class` en `GlobalExceptionHandler`.** Como me explicó Claude, capturaría también el `AccessDeniedException` de `@PreAuthorize` y el 403 acabaría siendo un 500.
- **JWT en `localStorage`.** Es lo más sencillo y vale para esta práctica, pero cualquier JavaScript de la página puede leerlo, así que un ataque XSS podría robarlo. Una alternativa serían las cookies `HttpOnly`, pero necesitan más configuración y no las hemos visto. Para reducir el riesgo, el frontend escribe los datos con `textContent` y nunca con `innerHTML`.
- **Frontend servido desde Spring Boot** (`src/main/resources/static`). Como está en el mismo origen que la API, no hace falta configurar CORS. Solo el HTML, el CSS y el JavaScript son públicos, y los datos siguen protegidos.
- **Los botones ocultos son solo por comodidad.** El frontend decide qué mostrar según los roles del token, pero no valida nada. Lo comprobé llamando a la API a mano con un usuario sin permisos y el backend respondió 403.
- **Clave de firma por propiedades**, con `${JWT_SECRET:valor}` para poder cambiarla con una variable de entorno sin tocar el código.

## Autor

 > Ruben Sanchez Delgado

## Videos de youtube

- **https://youtu.be/kcX_VQq2S50
- **https://youtu.be/9FWAmEIh5N0
