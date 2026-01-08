CREATE TABLE usuario_onboarding_progreso (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fk_usuario BIGINT NOT NULL,
    feature_key VARCHAR(50) NOT NULL,
    completado BOOLEAN NOT NULL DEFAULT FALSE,
    paso_actual INTEGER,
    fecha_completado TIMESTAMP,
    CONSTRAINT fk_onboarding_usuario FOREIGN KEY (fk_usuario) REFERENCES usuario(id),
    UNIQUE (fk_usuario, feature_key)
);
