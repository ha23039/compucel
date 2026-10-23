# Arquitectura de la app (fase 1)

En esta primera versión la app guarda los datos en el teléfono con Room y en la nube con Cloud Firestore. La idea que seguimos es simple: **el teléfono manda**. Todo se escribe primero en la base local y después se intenta enviar a la nube. Si no hay internet, el cambio queda marcado como pendiente y se envía más tarde.

## Capas

```
ui/            Pantallas (Activities y Fragments)
datos/         Repositorios y sincronización
  local/       Room: AppDatabase, entidades y DAOs
  remoto/      RemotoDatos (interfaz) y FirestoreRemoto
util/          Validaciones, BCrypt, cifrado local y sesión
```

Las pantallas solo hablan con los repositorios (`UsuarioRepositorio`, `ClienteRepositorio` y `Sincronizador`). Los repositorios usan Room y una implementación de `RemotoDatos`. Hoy esa implementación es `FirestoreRemoto`; en la fase 2 vamos a escribir otra que hable con la API REST de CompuCel, sin cambiar las pantallas.

Room nunca se usa en el hilo principal: los repositorios trabajan en un hilo aparte y las pantallas observan los datos con `LiveData`.

## Flujo de escritura sin conexión

1. Se guarda el registro en Room con `estadoSync = PENDIENTE`.
2. Si hay internet, se envía a Firestore. Solo cuando Firestore confirma, el registro pasa a `SINCRONIZADO`.
3. Si no hay internet, o la nube no responde en 15 segundos, el registro sigue `PENDIENTE`. En la lista se ve la nube ámbar y en Inicio el contador de pendientes.
4. Desde **Perfil** o **Sincronización**, el botón "Sincronizar ahora" reenvía todo lo pendiente.

Algunas reglas que tomamos:

- El id de cada cliente es un UUID que se genera en el teléfono y se usa igual en Firestore. Así, reintentar un envío no crea duplicados.
- Un cambio pendiente en el teléfono nunca se sobrescribe con lo que viene de la nube.
- Al eliminar sin conexión, el cliente se marca como eliminado y desaparece de la lista; se borra de Room cuando la nube confirma el borrado.
- Desactivamos la caché propia de Firestore para que la única base local sea Room y los pendientes se vean en la app.

## Contraseñas

- En Room y en Firestore la contraseña se guarda solo como **hash BCrypt** (`$2a$10$...`), el mismo formato que usa la API de la clase. Al iniciar sesión se compara la contraseña escrita con el hash; nunca se guarda en texto plano.
- La opción **Recordarme** guarda el correo y la contraseña en SharedPreferences, pero la contraseña va **cifrada con AES/GCM** y la clave de cifrado vive en el Android Keystore, que no permite sacarla del teléfono. Esto permite rellenar el campo al volver a abrir la app.

## Inicio de sesión sin conexión

El inicio de sesión revisa primero la base local. Si el usuario ya entró alguna vez en este teléfono, puede volver a hacerlo sin internet. El registro de una cuenta nueva sí necesita internet, porque hay que comprobar que el correo no exista en la nube.

## Mapa

La pantalla "Ubicación del taller" usa Google Maps (`SupportMapFragment`) con un marcador de CompuCel y el menú para cambiar el tipo de mapa. La clave de Maps está restringida a nuestro paquete y a la huella SHA-1 de cada equipo de desarrollo.
