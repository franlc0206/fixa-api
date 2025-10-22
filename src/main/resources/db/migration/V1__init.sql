-- Flyway V1: esquema inicial (parcial) - MySQL
-- Tabla base: usuario

CREATE TABLE IF NOT EXISTS usuario (
  id BIGINT NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(100) NULL,
  apellido VARCHAR(100) NULL,
  email VARCHAR(150) NOT NULL,
  telefono VARCHAR(30) NULL,
  password_hash VARCHAR(255) NULL,
  rol ENUM('superadmin','empresa','empleado','cliente') NOT NULL,
  activo TINYINT(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (id),
  UNIQUE KEY uk_usuario_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

