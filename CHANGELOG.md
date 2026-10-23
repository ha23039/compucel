# Registro de cambios

Aquí anotamos los cambios importantes de cada versión. El formato se basa en [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/) y usamos [versionado semántico](https://semver.org/lang/es/).

## [Sin publicar]

## [0.1.0] - 2026-10-24

Primera versión de la app, entregada en el Parcial 2.

### Agregado

- Pantalla de inicio (splash) con el logo de CompuCel.
- Inicio de sesión con correo y contraseña, validaciones y chip Recordarme. La contraseña recordada se guarda cifrada con el Android Keystore.
- Registro de usuarios con correo, nombre completo, contraseña y confirmación. Las contraseñas se guardan con BCrypt.
- Base de datos local con Room y remota con Cloud Firestore.
- Inicio con resumen de clientes, accesos rápidos, aviso de cambios pendientes y banner sin conexión.
- Menú de la barra superior con Perfil, Ubicación del taller y Cerrar sesión.
- Gestión de clientes: lista con búsqueda y filtros, detalle, agregar, editar, inactivar y eliminar.
- Funcionamiento sin conexión con cambios pendientes y botón "Sincronizar ahora".
- Perfil con cambio de nombre y de contraseña, apariencia clara, oscura o del sistema, y estado de sincronización.
- Pantalla de sincronización con pendientes por módulo y errores.
- Mapa con la ubicación del taller y los tipos de mapa.
- Base de la API REST con Spring Boot: entidades de usuario y cliente, BCrypt y Swagger.
- Reglas de Firestore y documento de arquitectura.
