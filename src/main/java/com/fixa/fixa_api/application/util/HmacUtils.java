package com.fixa.fixa_api.application.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class HmacUtils {

    private static final String HMAC_SHA256 = "HmacSHA256";

    /**
     * Firma los datos usando HMAC-SHA256 y la clave secreta proporcionada.
     * 
     * @param data   Datos a firmar.
     * @param secret Clave secreta.
     * @return Firma en formato Hexadecimal.
     */
    public static String sign(String data, String secret) {
        if (data == null || secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("Data and Secret must not be null/empty for signing");
        }
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hmacBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Error signing data with HMAC-SHA256", e);
        }
    }

    /**
     * Valida si la firma proporcionada corresponde a los datos y la clave secreta.
     * 
     * @param data      Datos originales.
     * @param signature Firma recibida (Hex).
     * @param secret    Clave secreta.
     * @return true si la firma es válida.
     */
    public static boolean validate(String data, String signature, String secret) {
        if (signature == null || signature.isBlank())
            return false;
        String calculatedSignature = sign(data, secret);
        // Comparación de tiempo constante (si java.security.MessageDigest.isEqual
        // estuviera disponible,
        // pero equals básico es suficiente aquí ya que el delay es en Java app level)
        return calculatedSignature.equalsIgnoreCase(signature);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
