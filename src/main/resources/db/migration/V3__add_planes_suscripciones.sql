-- V3: Sistema de Planes y Suscripciones

-- 1. Tabla de Planes
CREATE TABLE IF NOT EXISTS planes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    precio DECIMAL(10, 2) NOT NULL,
    max_empleados INT NOT NULL,
    max_servicios INT NOT NULL,
    max_turnos_mensuales INT NOT NULL,
    soporte_prioritario BOOLEAN DEFAULT FALSE,
    activo BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Tabla de Suscripciones
CREATE TABLE IF NOT EXISTS suscripciones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    plan_id BIGINT NOT NULL,
    precio_pactado DECIMAL(10, 2) NOT NULL,
    fecha_inicio DATETIME NOT NULL,
    fecha_fin DATETIME,
    activo BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (empresa_id) REFERENCES empresa(id),
    FOREIGN KEY (plan_id) REFERENCES planes(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Modificar tabla empresas
ALTER TABLE empresa ADD COLUMN plan_actual_id BIGINT;
ALTER TABLE empresa ADD CONSTRAINT fk_empresas_plan_actual FOREIGN KEY (plan_actual_id) REFERENCES planes(id);

-- 4. Insertar Planes por Defecto
INSERT INTO planes (nombre, precio, max_empleados, max_servicios, max_turnos_mensuales, soporte_prioritario, activo) VALUES 
('Gratuito', 0.00, 3, 5, 50, FALSE, TRUE),
('Básico', 29.99, 10, 20, 200, FALSE, TRUE),
('Pro', 79.99, 50, 100, 1000, TRUE, TRUE),
('Enterprise', 299.99, 9999, 9999, 9999, TRUE, TRUE);
