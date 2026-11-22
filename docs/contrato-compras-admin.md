
# Level-Up - Contrato de integración

## Responsables
- Administración: Mario Pino
- Compras: Axel

## Objetivo
Permitir que el Administrador consulte las órdenes
registradas por los clientes de Level-Up.

## Endpoints administrativos propuestos

### GET /api/v1/admin/ordenes
Consultar todas las órdenes registradas.

### GET /api/v1/admin/ordenes/{id}
Consultar el detalle de una orden específica.

### GET /api/v1/admin/ordenes/historial
Consultar el historial de compras.

## Datos pendientes de confirmar con Axel
- Identificador de la boleta y su tipo de dato.
- RUN del cliente.
- Fecha de compra.
- Monto total.
- Estado de la compra.
- Productos, cantidades y precios.

## Responsabilidades

### Axel - Compras
- Implementar Boleta y DetalleBoleta.
- Implementar los repositorios de compras.
- Definir la estructura de CompraResponse.
- Implementar el registro de compras.

### Mario - Administración
- Implementar ConsultaOrdenController.
- Implementar ConsultaOrdenService.
- Proteger los endpoints con rol Administrador.
- Implementar pruebas JUnit y MockMvc.

## Estado
Pendiente de confirmar el modelo de compras con Axel.

## Consideraciones
- No duplicar entidades.
- No modificar funcionalidades de otros integrantes.
- Utilizar datos reales de la base de datos.
- No exponer información sensible innecesaria.
