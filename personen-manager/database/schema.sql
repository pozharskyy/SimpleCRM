CREATE TABLE IF NOT EXISTS person (
    id         INT          AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    lastname   VARCHAR(100) NOT NULL,
    street     VARCHAR(200) NOT NULL,
    postalCode VARCHAR(20)  NOT NULL,
    city       VARCHAR(100) NOT NULL,
    country    VARCHAR(100) NOT NULL,
    CONSTRAINT uq_person UNIQUE (name, lastname, street)
);
