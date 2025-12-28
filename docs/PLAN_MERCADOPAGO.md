# Plan de Integración: Suscripciones con Mercado Pago

Este documento detalla la estrategia técnica para integrar Mercado Pago (MP) en **Fixe**, permitiendo que los usuarios compren planes y que el sistema cree automáticamente su empresa con las configuraciones del plan adquirido.

## 1. Flujo de Usuario Detallado
1. **Registro/Login**: El usuario debe estar autenticado.
2. **Selección de Plan**: El usuario elige un plan (ej: "Premium") en el frontend.
3. **Inicio de Suscripción**: El backend genera un link de Mercado Pago (Preapproval/Plan) incluyendo en el `external_reference` el `usuarioId` y el `planId`.
4. **Pago en MP**: El usuario completa el proceso en la interfaz de Mercado Pago.
5. **Webhook (IPN/Webhooks)**: Mercado Pago notifica al backend sobre el cambio de estado (ej: `authorized`).
6. **Procesamiento y Alta**: 
   - Se valida la integridad y se registra la notificación (Idempotencia).
   - Se crea la `Empresa` con los datos básicos del usuario.
   - Se vincula al `Usuario` como `OWNER` de esa empresa.
   - Se asigna el `Plan` comprado a la nueva empresa.

## 2. Componentes Técnicos a Implementar

### A. Capa de Dominio e Infraestructura
- **Log de Notificaciones**: Nueva tabla `mp_notification_log` para guardar los IDs de notificación procesados. Esto garantiza que si MP envía la misma notificación dos veces, no creamos la empresa dos veces.
- **Mercado Pago Adapter**: Componente en `infrastructure/out/mercadopago` que se comunique con la API de MP usando `RestTemplate` y el `AccessToken` de la App "Fix".

### B. Capa de Aplicación (Lógica de Negocio)
- **MercadoPagoSuscripcionService**:
  - `prepararSuscripcion(usuarioId, planId)`: Genera el `init_point` (URL de MP).
  - `procesarWebhook(payload)`:
    - Verifica si la notificación ya fue procesada.
    - Consulta el estado real en la API de MP (seguridad).
    - Si el estado es exitoso:
      1. Crea la `Empresa` (usando `EmpresaService`).
      2. Crea la `Suscripcion` interna.
      3. Actualiza el rol del usuario a `EMPRESA` (si es necesario).
      4. Vincula al usuario con la empresa como `OWNER`.

### C. Capa Web (Endpoints)
- `POST /api/backoffice/mercadopago/init`: Para iniciar el flujo desde el backoffice del usuario.
- `POST /api/public/mercadopago/webhook`: Endpoint público para recibir las notificaciones de MP.

## 3. Manejo de Errores e Idempotencia
- **Idempotencia**: Antes de procesar cualquier alta, se inserta el `id_notificacion` en la base de datos con un `UNIQUE constraint`. Si falla por duplicado, se responde `200 OK` a MP sin repetir el proceso.
- **Transaccionalidad**: El alta de la empresa, el vínculo del usuario y la asignación del plan se ejecutan dentro de una misma transacción base de datos. Si algo falla, se hace rollback completo.

## 4. Requerimientos de Configuración (application.yml)
- `mercadopago.access-token`: Token de producción/sandbox de la app Fix.
- `mercadopago.webhook-secret`: Para validar la firma de las notificaciones (opcional pero recomendado).
- `mercadopago.back-urls`: URLs de retorno al frontend (`success`, `failure`, `pending`).

---
Este plan prioriza la consistencia de datos y la seguridad, asegurando que cada pago resulte en una configuración de empresa válida y única.
