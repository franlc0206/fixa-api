package com.fixa.fixa_api.infrastructure.out.sms;

import com.fixa.fixa_api.domain.service.SmsServicePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Adapter que implementa el puerto SmsServicePort.
 * Siguiendo arquitectura hexagonal, este adapter:
 * - Implementa la interface del dominio (puerto)
 * - Encapsula la lógica de envío de SMS/WhatsApp (Twilio u otro proveedor)
 * - Pertenece a la capa de infraestructura
 * 
 * IMPLEMENTACIÓN ACTUAL: Mock para desarrollo (solo loguea el código)
 * Para producción: descomentar la integración con Twilio y configurar credenciales.
 */
@Component
public class SmsServiceAdapter implements SmsServicePort {

    private static final Logger log = LoggerFactory.getLogger(SmsServiceAdapter.class);

    @Value("${sms.mock.enabled:true}")
    private boolean mockEnabled;

    @Value("${sms.twilio.account-sid:}")
    private String twilioAccountSid;

    @Value("${sms.twilio.auth-token:}")
    private String twilioAuthToken;

    @Value("${sms.twilio.from-number:}")
    private String twilioFromNumber;

    @Override
    public boolean enviarCodigoVerificacion(String telefono, String codigo, String canal) {
        if (mockEnabled) {
            // Implementación MOCK para desarrollo
            log.info("═══════════════════════════════════════════════════════");
            log.info("📱 MOCK SMS SERVICE - Código de verificación");
            log.info("Teléfono: {}", telefono);
            log.info("Código: {}", codigo);
            log.info("Canal: {}", canal);
            log.info("Mensaje: Tu código de verificación es: {}", codigo);
            log.info("═══════════════════════════════════════════════════════");
            return true;
        } else {
            // TODO: Implementar integración real con Twilio
            // Ejemplo:
            /*
            try {
                Twilio.init(twilioAccountSid, twilioAuthToken);
                
                Message message = Message.creator(
                    new PhoneNumber(telefono),
                    new PhoneNumber(twilioFromNumber),
                    "Tu código de verificación es: " + codigo
                ).create();
                
                log.info("SMS enviado exitosamente. SID: {}", message.getSid());
                return true;
            } catch (Exception e) {
                log.error("Error al enviar SMS: {}", e.getMessage(), e);
                return false;
            }
            */
            
            log.warn("SMS Service configurado en modo REAL pero no implementado aún");
            log.warn("Configure sms.mock.enabled=true en application.properties para usar modo mock");
            return false;
        }
    }
}
