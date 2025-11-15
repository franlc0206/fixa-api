-- Flyway V2: Agregar tabla de valoraciones
-- Permite a usuarios registrados valorar empresas después de completar un turno

CREATE TABLE IF NOT EXISTS valoracion (
  id BIGINT NOT NULL AUTO_INCREMENT,
  fk_empresa BIGINT NOT NULL,
  fk_usuario BIGINT NOT NULL,
  fk_turno BIGINT NOT NULL,
  puntuacion INT NOT NULL,
  resena TEXT NULL,
  fecha_creacion DATETIME NOT NULL,
  activo TINYINT(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (id),
  UNIQUE KEY uk_valoracion_turno (fk_turno),
  INDEX idx_valoracion_empresa (fk_empresa),
  INDEX idx_valoracion_usuario (fk_usuario),
  CONSTRAINT fk_valoracion_empresa FOREIGN KEY (fk_empresa) REFERENCES empresa(id) ON DELETE CASCADE,
  CONSTRAINT fk_valoracion_usuario FOREIGN KEY (fk_usuario) REFERENCES usuario(id) ON DELETE CASCADE,
  CONSTRAINT fk_valoracion_turno FOREIGN KEY (fk_turno) REFERENCES turno(id) ON DELETE CASCADE,
  CONSTRAINT chk_valoracion_puntuacion CHECK (puntuacion >= 0 AND puntuacion <= 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
