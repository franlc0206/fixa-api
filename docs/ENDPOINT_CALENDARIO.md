# 📅 Endpoint Calendario BackOffice - Documentación Completa

**Última actualización**: 2025-11-04 20:25:00

---

## 📍 Información General

**Endpoint**: `GET /api/backoffice/calendario`  
**Base URL**: `http://localhost:8080`  
**Autenticación**: ✅ **Requerida** (HTTP Basic Auth)  
**Roles permitidos**: `SUPERADMIN`, `EMPRESA`, `EMPLEADO`

---

## 🔐 Autenticación

### Header Requerido

```http
Authorization: Basic {base64(email:password)}
```

### Ejemplo

Si tu usuario es:
- Email: `franslopezcortes@gmail.com`
- Password: `123456`

Entonces:
```javascript
// JavaScript
const credentials = btoa('franslopezcortes@gmail.com:123456');
// Resultado: ZnJhbnNsb3Blempjb3J0ZXNAZ21haWwuY29tOjEyMzQ1Ng==

// Header final
Authorization: Basic ZnJhbnNsb3Blempjb3J0ZXNAZ21haWwuY29tOjEyMzQ1Ng==
```

**⚠️ IMPORTANTE**: Sin este header, recibirás `401 Unauthorized` con el mensaje:
```json
{
  "code": "NO_AUTENTICADO",
  "message": "Usuario no autenticado"
}
```

---

## 📥 Request

### URL Completa

```
GET http://localhost:8080/api/backoffice/calendario?desde={ISO_DATETIME}&hasta={ISO_DATETIME}&empleadoId={LONG}&estados={STRING}
```

### Headers

| Header | Valor | Requerido |
|--------|-------|-----------|
| `Authorization` | `Basic {base64}` | ✅ Sí |
| `Accept` | `application/json` | ⚪ Opcional |

### Query Parameters

| Parámetro | Tipo | Requerido | Descripción | Ejemplo |
|-----------|------|-----------|-------------|---------|
| `desde` | `String` (ISO 8601) | ❌ No | Fecha/hora inicio del rango | `2025-10-28T00:00:00` |
| `hasta` | `String` (ISO 8601) | ❌ No | Fecha/hora fin del rango | `2025-12-04T23:59:59` |
| `empleadoId` | `Long` | ❌ No | Filtrar turnos de un empleado específico | `5` |
| `estados` | `String` | ❌ No | Estados separados por coma | `CONFIRMADO,PENDIENTE` |

#### Detalles de Parámetros

**`desde` y `hasta`** (Fechas ISO 8601):
- Formato: `YYYY-MM-DDTHH:mm:ss` o `YYYY-MM-DDTHH:mm:ss.SSSZ`
- Si no se envían: Backend usa inicio/fin del mes actual
- Zona horaria: UTC (agregar `Z` al final) o local (sin `Z`)

Ejemplos válidos:
```
2025-11-01T00:00:00
2025-11-30T23:59:59
2025-11-15T14:30:00.000Z
```

**`empleadoId`**:
- Si se envía: Solo turnos de ese empleado
- Si se omite: Turnos de todos los empleados de la empresa

**`estados`**:
- ⚠️ **IMPORTANTE**: Es un **String** con estados separados por **coma** (NO un array)
- Estados válidos: `PENDIENTE`, `CONFIRMADO`, `CANCELADO`, `COMPLETADO`, `REALIZADO`, `PENDIENTE_APROBACION`
- Si se omite: Todos los estados

Ejemplos:
```
✅ Correcto: estados=CONFIRMADO,PENDIENTE
✅ Correcto: estados=CONFIRMADO
✅ Correcto: estados=PENDIENTE,PENDIENTE_APROBACION,CONFIRMADO
❌ Incorrecto: estados[]=CONFIRMADO&estados[]=PENDIENTE (NO es un array)
```

---

## 📤 Response

### Status Codes

| Code | Descripción |
|------|-------------|
| `200 OK` | ✅ Turnos obtenidos correctamente |
| `401 Unauthorized` | ❌ No autenticado (falta header Authorization) |
| `403 Forbidden` | ❌ Usuario sin empresa asociada |
| `500 Internal Server Error` | ❌ Error del servidor |

### Response Body (200 OK)

Retorna un **array de eventos** en formato compatible con **FullCalendar v6**:

```json
[
  {
    "id": 15,
    "title": "Juan Pérez - Corte de cabello",
    "start": "2025-11-10T14:00:00",
    "end": "2025-11-10T14:30:00",
    "backgroundColor": "#28a745",
    "borderColor": "#28a745",
    "textColor": "#ffffff",
    "allDay": false,
    "estado": "CONFIRMADO",
    "clienteNombre": "Juan Pérez",
    "clienteTelefono": "+5491112345678",
    "servicioNombre": "Corte de cabello",
    "empleadoNombre": "Manuel García",
    "empleadoId": 5,
    "servicioId": 10,
    "requiereValidacion": true,
    "telefonoValidado": true
  },
  {
    "id": 16,
    "title": "María López - Manicure",
    "start": "2025-11-10T15:00:00",
    "end": "2025-11-10T16:00:00",
    "backgroundColor": "#ffc107",
    "borderColor": "#ffc107",
    "textColor": "#000000",
    "allDay": false,
    "estado": "PENDIENTE",
    "clienteNombre": "María López",
    "clienteTelefono": "+5491123456789",
    "servicioNombre": "Manicure",
    "empleadoNombre": "Ana Martínez",
    "empleadoId": 6,
    "servicioId": 11,
    "requiereValidacion": false,
    "telefonoValidado": false
  }
]
```

### Response Body (401 Unauthorized)

```json
{
  "code": "NO_AUTENTICADO",
  "message": "Usuario no autenticado",
  "details": null
}
```

### Response Body (403 Forbidden)

```json
{
  "code": "NO_EMPRESA_ASOCIADA",
  "message": "No estás asociado a ninguna empresa",
  "details": null
}
```

---

## 🎨 Colores por Estado

Los eventos vienen con colores predefinidos según el estado del turno:

| Estado | Color | Código Hex | Descripción |
|--------|-------|------------|-------------|
| `CONFIRMADO` | 🟢 Verde | `#28a745` | Turno confirmado |
| `PENDIENTE` | 🟡 Amarillo | `#ffc107` | Turno pendiente |
| `PENDIENTE_APROBACION` | 🟠 Naranja | `#fd7e14` | Requiere aprobación |
| `CANCELADO` | 🔴 Rojo | `#dc3545` | Turno cancelado |
| `COMPLETADO` o `REALIZADO` | ⚫ Gris | `#6c757d` | Turno finalizado |

---

## 🔍 Campos del Evento (CalendarioEventoDTO)

### Campos de FullCalendar (estándar)

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `id` | `Long` | ID único del turno |
| `title` | `String` | Título del evento (ej: "Juan Pérez - Corte") |
| `start` | `String` (ISO) | Fecha/hora inicio (formato: `YYYY-MM-DDTHH:mm:ss`) |
| `end` | `String` (ISO) | Fecha/hora fin |
| `backgroundColor` | `String` | Color de fondo del evento |
| `borderColor` | `String` | Color del borde |
| `textColor` | `String` | Color del texto |
| `allDay` | `Boolean` | Siempre `false` (turnos tienen horario) |

### Campos Personalizados

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `estado` | `String` | Estado del turno (CONFIRMADO, PENDIENTE, etc.) |
| `clienteNombre` | `String` | Nombre completo del cliente |
| `clienteTelefono` | `String` | Teléfono del cliente |
| `servicioNombre` | `String` | Nombre del servicio |
| `servicioId` | `Long` | ID del servicio |
| `empleadoNombre` | `String` | Nombre completo del empleado |
| `empleadoId` | `Long` | ID del empleado |
| `requiereValidacion` | `Boolean` | Si la empresa requiere validación telefónica |
| `telefonoValidado` | `Boolean` | Si el teléfono fue validado |

---

## 🧪 Ejemplos de Peticiones

### Ejemplo 1: Obtener todos los turnos del mes actual

```http
GET http://localhost:8080/api/backoffice/calendario
Authorization: Basic ZnJhbnNsb3Blempjb3J0ZXNAZ21haWwuY29tOjEyMzQ1Ng==
```

### Ejemplo 2: Filtrar por rango de fechas

```http
GET http://localhost:8080/api/backoffice/calendario?desde=2025-11-01T00:00:00&hasta=2025-11-30T23:59:59
Authorization: Basic ZnJhbnNsb3Blempjb3J0ZXNAZ21haWwuY29tOjEyMzQ1Ng==
```

### Ejemplo 3: Solo turnos confirmados y pendientes

```http
GET http://localhost:8080/api/backoffice/calendario?desde=2025-11-01T00:00:00&hasta=2025-11-30T23:59:59&estados=CONFIRMADO,PENDIENTE
Authorization: Basic ZnJhbnNsb3Blempjb3J0ZXNAZ21haWwuY29tOjEyMzQ1Ng==
```

### Ejemplo 4: Turnos de un empleado específico

```http
GET http://localhost:8080/api/backoffice/calendario?empleadoId=5&estados=CONFIRMADO,PENDIENTE
Authorization: Basic ZnJhbnNsb3Blempjb3J0ZXNAZ21haWwuY29tOjEyMzQ1Ng==
```

### Ejemplo 5: Con fechas UTC (timezone Z)

```http
GET http://localhost:8080/api/backoffice/calendario?desde=2025-10-28T23:00:00.000Z&hasta=2025-12-04T22:59:59.999Z&estados=CONFIRMADO,PENDIENTE,PENDIENTE_APROBACION
Authorization: Basic ZnJhbnNsb3Blempjb3J0ZXNAZ21haWwuY29tOjEyMzQ1Ng==
```

---

## 🔧 Integración Frontend (JavaScript/TypeScript)

### Con Fetch API

```javascript
async function getCalendario(desde, hasta, empleadoId, estados) {
  const email = 'franslopezcortes@gmail.com';
  const password = '123456';
  const credentials = btoa(`${email}:${password}`);
  
  const params = new URLSearchParams();
  if (desde) params.append('desde', desde);
  if (hasta) params.append('hasta', hasta);
  if (empleadoId) params.append('empleadoId', empleadoId);
  if (estados) params.append('estados', estados); // String: "CONFIRMADO,PENDIENTE"
  
  const url = `http://localhost:8080/api/backoffice/calendario?${params.toString()}`;
  
  const response = await fetch(url, {
    method: 'GET',
    headers: {
      'Authorization': `Basic ${credentials}`,
      'Accept': 'application/json',
    },
  });
  
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Error al obtener calendario');
  }
  
  return await response.json();
}

// Uso
const eventos = await getCalendario(
  '2025-11-01T00:00:00',
  '2025-11-30T23:59:59',
  null,
  'CONFIRMADO,PENDIENTE'
);
```

### Con Axios

```javascript
import axios from 'axios';

const http = axios.create({
  baseURL: 'http://localhost:8080',
});

// Interceptor para agregar Basic Auth automáticamente
http.interceptors.request.use((config) => {
  const email = localStorage.getItem('user_email');
  const password = localStorage.getItem('user_password');
  
  if (email && password) {
    const credentials = btoa(`${email}:${password}`);
    config.headers.Authorization = `Basic ${credentials}`;
  }
  
  return config;
});

// Función para obtener calendario
async function getCalendario(filtros) {
  const { data } = await http.get('/api/backoffice/calendario', {
    params: {
      desde: filtros.desde,
      hasta: filtros.hasta,
      empleadoId: filtros.empleadoId,
      estados: filtros.estados, // String: "CONFIRMADO,PENDIENTE"
    },
  });
  
  return data;
}

// Uso
const eventos = await getCalendario({
  desde: '2025-11-01T00:00:00',
  hasta: '2025-11-30T23:59:59',
  estados: 'CONFIRMADO,PENDIENTE',
});
```

### Con React Query

```typescript
import { useQuery } from '@tanstack/react-query';
import { getCalendario } from './api';

interface CalendarioFiltros {
  desde?: string;
  hasta?: string;
  empleadoId?: number;
  estados?: string; // "CONFIRMADO,PENDIENTE"
}

export const useCalendario = (filtros: CalendarioFiltros) => {
  return useQuery({
    queryKey: ['calendario', filtros],
    queryFn: () => getCalendario(filtros),
    enabled: !!localStorage.getItem('user_email'), // Solo si está autenticado
    staleTime: 1000 * 60 * 2, // 2 minutos
    refetchOnWindowFocus: true,
  });
};

// Uso en componente
function CalendarioPage() {
  const { data: eventos, isLoading, error } = useCalendario({
    desde: '2025-11-01T00:00:00',
    hasta: '2025-11-30T23:59:59',
    estados: 'CONFIRMADO,PENDIENTE',
  });

  if (isLoading) return <div>Cargando...</div>;
  if (error) return <div>Error: {error.message}</div>;

  return <FullCalendar events={eventos} />;
}
```

---

## 🎯 Integración con FullCalendar v6

### Instalación

```bash
npm install @fullcalendar/react @fullcalendar/daygrid @fullcalendar/timegrid @fullcalendar/interaction
```

### Ejemplo de Componente React

```tsx
import FullCalendar from '@fullcalendar/react';
import dayGridPlugin from '@fullcalendar/daygrid';
import timeGridPlugin from '@fullcalendar/timegrid';
import interactionPlugin from '@fullcalendar/interaction';
import { useCalendario } from './hooks/useCalendario';

function CalendarioBackoffice() {
  const [filtros, setFiltros] = useState({
    desde: '2025-11-01T00:00:00',
    hasta: '2025-11-30T23:59:59',
    estados: 'CONFIRMADO,PENDIENTE',
  });

  const { data: eventos = [], isLoading } = useCalendario(filtros);

  if (isLoading) return <div>Cargando calendario...</div>;

  return (
    <div>
      <FullCalendar
        plugins={[dayGridPlugin, timeGridPlugin, interactionPlugin]}
        initialView="dayGridMonth"
        headerToolbar={{
          left: 'prev,next today',
          center: 'title',
          right: 'dayGridMonth,timeGridWeek,timeGridDay',
        }}
        events={eventos}
        eventClick={(info) => {
          console.log('Turno clickeado:', info.event);
          // Abrir modal con detalles del turno
        }}
        locale="es"
        buttonText={{
          today: 'Hoy',
          month: 'Mes',
          week: 'Semana',
          day: 'Día',
        }}
      />
    </div>
  );
}
```

---

## ⚠️ Errores Comunes y Soluciones

### Error 1: 401 Unauthorized

**Síntoma**:
```json
{
  "code": "NO_AUTENTICADO",
  "message": "Usuario no autenticado"
}
```

**Causas posibles**:
1. ❌ Falta el header `Authorization`
2. ❌ Credenciales incorrectas en el header
3. ❌ Email o password incorrectos
4. ❌ Usuario no existe en la base de datos

**Solución**:
```javascript
// Verificar que el header se esté enviando
console.log('Authorization header:', request.headers.Authorization);

// Verificar credenciales
const email = 'franslopezcortes@gmail.com';
const password = '123456';
const credentials = btoa(`${email}:${password}`);
console.log('Credentials Base64:', credentials);
```

### Error 2: 403 Forbidden

**Síntoma**:
```json
{
  "code": "NO_EMPRESA_ASOCIADA",
  "message": "No estás asociado a ninguna empresa"
}
```

**Causa**: El usuario no tiene ninguna empresa asociada activa.

**Solución**: Asignar una empresa al usuario desde el SuperAdmin.

### Error 3: Fechas en formato incorrecto

**Síntoma**: Backend no interpreta las fechas correctamente.

**Formatos válidos**:
```
✅ 2025-11-01T00:00:00
✅ 2025-11-01T00:00:00.000Z
❌ 2025-11-01 (falta hora)
❌ 11/01/2025 (formato incorrecto)
```

### Error 4: Estados como array

**Síntoma**: Estados no se envían correctamente.

**Formato incorrecto** (NO USAR):
```javascript
// ❌ NO: Como array en query params
?estados[]=CONFIRMADO&estados[]=PENDIENTE
```

**Formato correcto**:
```javascript
// ✅ SÍ: Como string separado por comas
?estados=CONFIRMADO,PENDIENTE
```

---

## 🔄 Flujo Completo de la Petición

```
Frontend                              Backend
   |                                     |
   |  1. Login exitoso                  |
   |  Guardar email + password          |
   |                                     |
   |  2. Construir header Authorization |
   |     Basic base64(email:password)   |
   |                                     |
   |  3. GET /api/backoffice/calendario |
   |     + Headers + Query params    ---->  4. Recibir petición
   |                                     |
   |                                     |  5. Spring Security valida
   |                                     |     Authorization header
   |                                     |
   |                                     |  6. BackofficeAccessFilter
   |                                     |     verifica empresa asociada
   |                                     |
   |                                     |  7. CalendarioQueryService
   |                                     |     obtiene turnos de BD
   |                                     |
   |  8. Recibir array de eventos  <----  9. Mapear a DTOs FullCalendar
   |                                     |     y retornar 200 OK
   |  10. Mostrar en FullCalendar       |
   |                                     |
```

---

## 📝 Checklist Frontend

Antes de consumir el endpoint, verifica:

- [ ] ✅ Usuario hizo login exitosamente
- [ ] ✅ Guardaste `email` y `password` en localStorage/sessionStorage
- [ ] ✅ Axios interceptor agrega `Authorization: Basic ...` automáticamente
- [ ] ✅ Parámetro `estados` es un **String** (no array)
- [ ] ✅ Fechas en formato ISO 8601: `YYYY-MM-DDTHH:mm:ss`
- [ ] ✅ FullCalendar v6 instalado y configurado
- [ ] ✅ Manejador de errores 401 (redirigir a login)
- [ ] ✅ Manejador de errores 403 (mostrar mensaje)

---

## 🚀 Estado del Backend

| Componente | Estado |
|------------|--------|
| **Endpoint** | ✅ Implementado |
| **Autenticación** | ✅ HTTP Basic Auth |
| **Filtro BackOffice** | ✅ Verifica empresa asociada |
| **Formato Response** | ✅ Compatible FullCalendar v6 |
| **Colores por estado** | ✅ Predefinidos |
| **CORS** | ✅ Permitido desde `localhost:5173` |

---

## 📞 Contacto

**Backend Developer**: Francisco López  
**Última actualización**: 2025-11-04  
**Versión API**: v1.0

---

**⚠️ RECORDATORIO IMPORTANTE**: El backend debe estar **reiniciado** para que los últimos cambios de seguridad se apliquen. Si el calendario sigue dando 401, verificar que la aplicación Spring Boot esté corriendo con el código más reciente.
