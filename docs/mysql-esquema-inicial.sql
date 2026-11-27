-- Level-Up Gamer: esquema inicial para una base MySQL vacía.
-- Ejecutar después de seleccionar la base de datos de la aplicación.
-- La columna version_seguridad ya está incluida en este esquema.

CREATE TABLE usuarios (
    run               VARCHAR(9)   NOT NULL,
    nombre            VARCHAR(50)  NOT NULL,
    apellidos         VARCHAR(100) NOT NULL,
    correo            VARCHAR(100) NOT NULL,
    fecha_nacimiento  DATE         NOT NULL,
    region            VARCHAR(255) NOT NULL,
    comuna            VARCHAR(255) NOT NULL,
    direccion         VARCHAR(300) NOT NULL,
    password_hash     VARCHAR(255) NOT NULL,
    rol               ENUM('Cliente', 'Vendedor', 'Administrador') NOT NULL,
    version_seguridad VARCHAR(36)  NOT NULL,

    PRIMARY KEY (run),
    CONSTRAINT uq_usuarios_correo UNIQUE (correo),
    CONSTRAINT uq_usuarios_version_seguridad UNIQUE (version_seguridad)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE refresh_tokens (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    usuario_run VARCHAR(9)   NOT NULL,
    token_hash  VARCHAR(64)  NOT NULL,
    creado_en   TIMESTAMP(6) NOT NULL,
    vence_en    TIMESTAMP(6) NOT NULL,
    revocado_en TIMESTAMP(6) NULL DEFAULT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uq_refresh_tokens_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_usuario
        FOREIGN KEY (usuario_run) REFERENCES usuarios (run)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE categorias (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    nombre      VARCHAR(100) NOT NULL,
    descripcion VARCHAR(300) NULL,

    PRIMARY KEY (id),
    CONSTRAINT uq_categorias_nombre UNIQUE (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE productos (
    id              BIGINT         NOT NULL AUTO_INCREMENT,
    codigo          VARCHAR(50)    NULL,
    nombre          VARCHAR(150)   NOT NULL,
    descripcion     VARCHAR(1000)  NULL,
    precio          DECIMAL(12,2)  NOT NULL,
    precio_anterior DECIMAL(12,2)  NULL,
    stock           INT            NOT NULL,
    stock_critico   INT            NULL,
    imagen          VARCHAR(500)   NULL,
    categoria_id    BIGINT         NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT uq_productos_codigo
        UNIQUE (codigo),

    CONSTRAINT fk_productos_categoria
        FOREIGN KEY (categoria_id)
        REFERENCES categorias (id)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- Carrito persistente asociado a cada usuario.
CREATE TABLE carrito_items (
    id          BIGINT     NOT NULL AUTO_INCREMENT,
    usuario_run VARCHAR(9) NOT NULL,
    producto_id BIGINT     NOT NULL,
    cantidad    INT        NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT uk_carrito_usuario_producto
        UNIQUE (usuario_run, producto_id),

    CONSTRAINT fk_carrito_usuario
        FOREIGN KEY (usuario_run)
        REFERENCES usuarios (run)
        ON DELETE CASCADE,

    CONSTRAINT fk_carrito_producto
        FOREIGN KEY (producto_id)
        REFERENCES productos (id)
        ON DELETE CASCADE

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE contactos (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    nombre      VARCHAR(100) NOT NULL,
    correo      VARCHAR(100) NULL,
    comentario  VARCHAR(500) NOT NULL,
    fecha_envio DATETIME(6)  NOT NULL,
    atendido    BIT(1)       NOT NULL,

    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- La orden conserva los datos del cliente usados al comprar.
CREATE TABLE ordenes (
    id                   BIGINT        NOT NULL AUTO_INCREMENT,
    fecha                TIMESTAMP(6)  NOT NULL,
    cliente_run          VARCHAR(9)    NOT NULL,
    cliente_nombre       VARCHAR(150)  NOT NULL,
    cliente_correo       VARCHAR(100)  NOT NULL,
    direccion            VARCHAR(300)  NOT NULL,
    entrega              VARCHAR(40)   NOT NULL,
    estado               VARCHAR(20)   NOT NULL,
    subtotal             DECIMAL(19,2) NOT NULL,
    porcentaje_descuento INT           NOT NULL,
    descuento            DECIMAL(19,2) NOT NULL,
    costo_envio          DECIMAL(19,2) NOT NULL,
    total                DECIMAL(19,2) NOT NULL,

    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Cada fila conserva el nombre y el precio del producto al comprar.
-- La posición identifica la línea dentro de su propia orden.
CREATE TABLE orden_detalles (
    orden_id        BIGINT        NOT NULL,
    posicion        INT           NOT NULL,
    producto_id     BIGINT        NOT NULL,
    producto_nombre VARCHAR(150)  NOT NULL,
    precio_unitario DECIMAL(12,2) NOT NULL,
    cantidad        INT           NOT NULL,

    PRIMARY KEY (orden_id, posicion),
    CONSTRAINT fk_orden_detalles_orden
        FOREIGN KEY (orden_id) REFERENCES ordenes (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;