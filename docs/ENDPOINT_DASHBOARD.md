# Endpoint Dashboard Métricas

Este endpoint permite obtener métricas clave para el dashboard del backoffice.

## URL
`GET /api/backoffice/dashboard/metrics`

## Autenticación
Requiere Token JWT de usuario autenticado (Backoffice).

## Parámetros (Query Params)

| Parámetro | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `empresaId` | `Long` | No | ID de la empresa. Si no se envía, se usa la primera empresa activa del usuario. |
| `inicio` | `Date` | No | Fecha de inicio del periodo (ISO 8601: `YYYY-MM-DD`). Default: Inicio del mes actual. |
| `fin` | `Date` | No | Fecha de fin del periodo (ISO 8601: `YYYY-MM-DD`). Default: Fin del mes actual. |

## Respuesta Exitosa (200 OK)

```json
{
  "totalTurnosMes": 150,
  "ingresosEstimadosMes": 450000.50,
  "turnosPorEstado": {
    "CONFIRMADO": 120,
    "PENDIENTE": 10,
    "CANCELADO": 5,
    "COMPLETADO": 15
  },
  "topEmpleados": [
    {
      "empleadoId": 1,
      "nombre": "Juan",
      "apellido": "Perez",
      "cantidadTurnos": 45
    },
    {
      "empleadoId": 2,
      "nombre": "Maria",
      "apellido": "Gomez",
      "cantidadTurnos": 30
    }
  ],
  "topServicios": [
    {
      "servicioId": 10,
      "nombre": "Corte de Pelo",
      "cantidadTurnos": 80,
      "ingresosGenerados": 240000.00
    },
    {
      "servicioId": 12,
      "nombre": "Barba",
      "cantidadTurnos": 40,
      "ingresosGenerados": 80000.00
    }
  ],
  "turnosPorMesUltimoAno": {
    "2023-12": 100,
    "2024-01": 110,
    "2024-02": 95,
    "..." : 0
  }
}
```

## Errores Comunes

- `401 Unauthorized`: No se envió token o es inválido.
- `403 Forbidden`: El usuario no tiene acceso a la empresa solicitada.
