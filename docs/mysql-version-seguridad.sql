-- Ejecutar una sola vez en una base MySQL existente, con la API detenida.
-- Las sesiones anteriores requieren iniciar sesión nuevamente.
ALTER TABLE usuarios ADD COLUMN version_seguridad VARCHAR(36);
UPDATE usuarios SET version_seguridad = UUID() WHERE version_seguridad IS NULL;
ALTER TABLE usuarios
    MODIFY COLUMN version_seguridad VARCHAR(36) NOT NULL,
    ADD CONSTRAINT uk_usuarios_version_seguridad UNIQUE (version_seguridad);
DELETE FROM refresh_tokens;
