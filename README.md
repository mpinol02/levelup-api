# Level-Up Gamer | Backend EV3
Base de trabajo para Mario y Axel. Abrir esta carpeta (la que contiene pom.xml) en VS Code o IntelliJ. Frontend separado: tienda-react.

## Windows · primera ejecución
Requisitos: JDK 21 LTS y Maven 3.9 o superior en PATH. Verificar con java -version, javac -version y mvn -version. La primera compilación descarga dependencias.
1. En esta carpeta ejecutar `mvn test`.
2. Ejecutar `mvn spring-boot:run "-Dspring-boot.run.profiles=local"`.
3. Abrir http://localhost:8080/api/v1/estado y comprobar estado OK. Detener con Ctrl+C.

## Actualizar una base MySQL existente

Con la API detenida, ejecuta una vez [la migración de seguridad JWT](docs/mysql-version-seguridad.sql)
antes de arrancar esta versión en `dev` o `prod`. La migración asigna una versión única a cada cuenta
y elimina los tokens de renovación existentes. Los usuarios deben volver a iniciar sesión.
El perfil `local` crea el esquema en H2 al arrancar.

## Responsabilidades
Todo el código Java está en src/main/java/cl/levelup.
- Mario: usuario, auth, config, error, producto, categoria y admin. Admin consulta órdenes, historial y reportes.
- Axel: compra (Boleta, DetalleBoleta, repositorios, servicio, controlador y DTO). Calcula importes y actualiza stock.
- Ambos: coordinar campos de Producto y Boleta y JSON bajo /api/v1. Pruebas en src/test/java/cl/levelup.

## Alcance inicial
Funciona como punto de partida: arranque Spring Boot, ruta de estado y prueba de integración preparada. Los demás archivos son esqueletos; los tests vacíos no prueban funcionalidades. La seguridad solo habilita la ruta de estado. Swagger está incluido como dependencia pero sus rutas no están habilitadas todavía.
Local usa H2 en memoria solo para desarrollo inicial. Para MySQL usar perfil dev y definir DB_URL, DB_USER y DB_PASSWORD en el entorno con una base existente. Prod valida el esquema y requiere que ya esté creado. No subir credenciales.
Primer bloque Mario: entidad Usuario, Rol y UsuarioRepository. Después registro con contraseña hasheada, login y JWT. Primer bloque Axel: definir Boleta y DetalleBoleta con Mario antes de escribir la compra.