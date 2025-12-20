# Guía de Configuración: Webhooks de Mercado Pago

Para que el backend reciba las confirmaciones de pago y cree las empresas automáticamente, debés configurar el Webhook en tu Dashboard de Mercado Pago.

### 1. Acceder al Dashboard
Entrá a tu panel de aplicaciones en [Mercado Pago Developers](https://www.mercadopago.com.ar/developers/panel/notifications/webhooks).

### 2. Configurar el Endpoint
Hacé clic en **"Crear nuevo"** o editá tu aplicación **Fix** y completá los datos:

- **URL de producción**: `https://api.tu-dominio.com/api/public/mercadopago/webhook`
  > [!IMPORTANT]
  > Reemplazá `tu-dominio.com` por la URL real de tu servidor (donde está corriendo la API).
- **Eventos a suscribir**:
    - [x] **Suscripciones** (Preapproval) -> Es el más importante para el flujo de planes.
    - [x] **Pagos** (Payments) -> Opcional, pero recomendado para logs.
- **Versión de la API**: Usá siempre la más reciente.

### 3. Guardar y Probar
Una vez guardado, podés usar el botón **"Probar"** para enviar una notificación de prueba. 
- En el backend deberías ver un log indicando que llegó la notificación.
- Como es una prueba manual sin ID real, el backend simplemente la ignorará o logueará el error, pero confirmará que el canal está abierto.

---

### Seguridad (Opcional)
En la sección de Webhooks verás una **"Clave secreta"**. 
Si querés que el backend valide que la notificación realmente viene de Mercado Pago, guardá esa clave en tu entorno como `MP_WEBHOOK_SECRET` y avisame para implementar la verificación de firma. Por ahora, el backend usa el mecanismo de **"Verificación vía API"** (pregunta a MP por cada ID recibido), lo cual ya es seguro.
