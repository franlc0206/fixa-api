package com.fixa.fixa_api.application.service;

import com.fixa.fixa_api.domain.model.Turno;
import com.fixa.fixa_api.domain.repository.EmpresaRepositoryPort;
import com.fixa.fixa_api.domain.repository.ServicioRepositoryPort;
import com.fixa.fixa_api.domain.service.NotificationServicePort;
import com.fixa.fixa_api.infrastructure.security.CurrentUserService;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class TurnoNotificationService {

    private final NotificationServicePort notificationPort;
    private final ServicioRepositoryPort servicioPort;
    private final EmpresaRepositoryPort empresaPort;
    private final CurrentUserService currentUserService;

    public TurnoNotificationService(
            NotificationServicePort notificationPort,
            ServicioRepositoryPort servicioPort,
            EmpresaRepositoryPort empresaPort,
            CurrentUserService currentUserService) {
        this.notificationPort = notificationPort;
        this.servicioPort = servicioPort;
        this.empresaPort = empresaPort;
        this.currentUserService = currentUserService;
    }

    public void enviarNotificacionCreacion(Turno guardado) {
        try {
            var servicio = servicioPort.findById(guardado.getServicioId()).orElse(null);
            var empresa = empresaPort.findById(guardado.getEmpresaId()).orElse(null);

            if (servicio == null || empresa == null)
                return;

            Map<String, String> vars = new HashMap<>();
            vars.put("nombre", guardado.getClienteNombre());
            vars.put("servicio", servicio.getNombre());
            vars.put("fecha", guardado.getFechaHoraInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            vars.put("empresa", empresa.getNombre());

            String template = "Hola <b>{{nombre}}</b>, tu turno para <b>{{servicio}}</b> en <b>{{empresa}}</b> el día <b>{{fecha}}</b> ha sido registrado.\n\n"
                    + "Estado actual: <b>" + guardado.getEstado() + "</b>";
            notificationPort.sendEmail(guardado.getClienteEmail(), template, vars);
        } catch (Exception e) {
            // Loguear error
        }
    }

    public void enviarNotificacionReprogramacion(Turno guardado) {
        try {
            var servicio = servicioPort.findById(guardado.getServicioId()).orElse(null);
            var empresa = empresaPort.findById(guardado.getEmpresaId()).orElse(null);

            if (servicio == null || empresa == null)
                return;

            Map<String, String> vars = new HashMap<>();
            vars.put("nombre", guardado.getClienteNombre());
            vars.put("servicio", servicio.getNombre());
            vars.put("fecha", guardado.getFechaHoraInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            vars.put("empresa", empresa.getNombre());

            String template = "Hola <b>{{nombre}}</b>, tu turno para <b>{{servicio}}</b> en <b>{{empresa}}</b> ha sido <b>REPROGRAMADO</b> para el día <b>{{fecha}}</b>.\n\n"
                    + "Estado actual: <b>" + guardado.getEstado() + "</b>";
            notificationPort.sendEmail(guardado.getClienteEmail(), template, vars);
        } catch (Exception e) {
            // Loguear error
        }
    }

    public void enviarNotificacionAprobacion(Turno guardado) {
        try {
            var servicio = servicioPort.findById(guardado.getServicioId()).orElse(null);
            var empresa = empresaPort.findById(guardado.getEmpresaId()).orElse(null);

            Map<String, String> vars = new HashMap<>();
            vars.put("nombre", guardado.getClienteNombre());
            vars.put("servicio", servicio != null ? servicio.getNombre() : "Servicio");
            vars.put("fecha", guardado.getFechaHoraInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            vars.put("empresa", empresa != null ? empresa.getNombre() : "la empresa");

            String template = "¡Buenas noticias <b>{{nombre}}</b>! Tu turno para <b>{{servicio}}</b> en <b>{{empresa}}</b> el día <b>{{fecha}}</b> ha sido <b>CONFIRMADO</b>.\n\n"
                    + "¡Te esperamos!";
            notificationPort.sendEmail(guardado.getClienteEmail(), template, vars);
        } catch (Exception e) {
            // Loguear error
        }
    }

    public void enviarNotificacionCancelacion(Turno guardado, String motivo) {
        try {
            var currentUser = currentUserService.getCurrentUser().orElse(null);
            var servicio = servicioPort.findById(guardado.getServicioId()).orElse(null);
            var empresa = empresaPort.findById(guardado.getEmpresaId()).orElse(null);

            Map<String, String> vars = new HashMap<>();
            vars.put("nombre", guardado.getClienteNombre());
            vars.put("servicio", servicio != null ? servicio.getNombre() : "Servicio");
            vars.put("fecha", guardado.getFechaHoraInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            vars.put("empresa", empresa != null ? empresa.getNombre() : "la empresa");
            vars.put("motivo", motivo != null ? motivo : "No especificado");

            if (currentUser != null && "CLIENTE".equalsIgnoreCase(currentUser.getRol())) {
                String template = "El turno de <b>{{nombre}}</b> para <b>{{servicio}}</b> el día <b>{{fecha}}</b> ha sido <b>CANCELADO</b> por el cliente.\n\n"
                        + "Motivo: <i>{{motivo}}</i>";
                notificationPort.sendEmail(empresa != null ? empresa.getEmail() : null, template, vars);
            } else {
                String template = "Hola <b>{{nombre}}</b>, lamentamos informarte que tu turno para <b>{{servicio}}</b> en <b>{{empresa}}</b> el día <b>{{fecha}}</b> ha sido <b>CANCELADO</b>.\n\n"
                        + "Motivo: <i>{{motivo}}</i>";
                notificationPort.sendEmail(guardado.getClienteEmail(), template, vars);
            }
        } catch (Exception e) {
            // Loguear error
        }
    }
}
