# CompuCel

Aplicación móvil para la gestión de un taller de reparación de celulares y computadoras: clientes, órdenes de reparación, repuestos y técnicos, con datos guardados en el teléfono y sincronizados con la nube.

Es el proyecto de la asignatura Desarrollo de Aplicaciones Móviles II (DAM-235) de la Universidad de El Salvador, Facultad Multidisciplinaria de Occidente.

## Estado

Versión 0.3.0 (fase 1). Ver [ROADMAP.md](ROADMAP.md) y [CHANGELOG.md](CHANGELOG.md).

Lo que ya funciona en la app:

- Pantalla de inicio, inicio de sesión con la opción Recordarme y registro de usuarios.
- Contraseñas guardadas con BCrypt en Room y en Firestore.
- Inicio con resumen del taller, menú en la barra superior y barra de navegación.
- Clientes: agregar, buscar, ver, editar, inactivar y eliminar.
- Funcionamiento sin conexión: los cambios quedan pendientes y se envían con "Sincronizar ahora".
- Servicios: agregar, editar y eliminar con nombre, descripción, costo y tiempo aproximado.
- Perfil: cambio de nombre y de contraseña, apariencia clara u oscura y estado de la sincronización.
- Mapa con la ubicación del taller.

## Estructura del repositorio

| Carpeta | Contenido |
|---|---|
| `android/` | App Android nativa en Java |
| `api/` | API REST del proyecto (Spring Boot) |
| `docs/` | Arquitectura, reglas de Firestore y colección de Postman |

## Cómo compilar la app

Requisitos: Android Studio con el SDK 36 y JDK 17 o superior.

1. Descargar `google-services.json` desde la consola de Firebase del proyecto (Configuración del proyecto, app Android `sv.edu.ues.dam235.compucel`) y copiarlo en `android/app/`.
2. En `android/local.properties` agregar la clave de Google Maps:
   ```
   MAPS_API_KEY=tu_clave
   ```
   La clave está restringida por paquete y huella SHA-1. Para usarla en otro equipo hay que agregar su huella en Google Cloud (Credenciales). La huella de depuración se obtiene con:
   ```
   keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
   ```
3. Abrir la carpeta `android/` en Android Studio y ejecutar la app, o desde la terminal:
   ```
   cd android
   ./gradlew assembleDebug
   ```

Ninguno de los dos archivos se sube al repositorio.

## Cómo correr la API

Requisitos: JDK 21 y una base MariaDB.

```
cd api
DB_URL=jdbc:mariadb://localhost:3306/compucel DB_USER=usuario DB_PASS=clave ./mvnw spring-boot:run
```

- Información de la API: `http://localhost:8085/api/v1/compucel/info`
- Documentación (Swagger): `http://localhost:8085/api/v1/compucel/swagger-ui.html`
- Clientes: `GET /clientes`, `GET /clientes/{id}`, `PUT /clientes/{id}` y `DELETE /clientes/{id}`. Por ahora estas rutas no piden token; la autenticación con JWT llega en la fase 2.
- La colección de Postman está en `docs/postman/CompuCel.postman_collection.json`.

## Pruebas

```
cd android && ./gradlew testDebugUnitTest connectedDebugAndroidTest
cd api && ./mvnw test
```

## Integrantes

| Carné | Nombre |
|---|---|
| ha23039 | Erick Enrique Hernández Aguillón |
| re20005 | Gerson David Ramirez Esteves |

Docente: Ing. Edenilson López.
