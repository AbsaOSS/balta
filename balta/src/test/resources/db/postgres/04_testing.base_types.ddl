DROP TABLE IF EXISTS testing.base_types;

CREATE TABLE IF NOT EXISTS testing.base_types
(
    long_type bigint,
    boolean_type boolean,
    char_type "char",
    string_type text COLLATE pg_catalog."default",
    int_type integer,
    double_type double precision,
    float_type real,
    bigdecimal_type numeric(25,10),
    date_type date,
    time_type time without time zone,
    timestamp_type timestamp without time zone,
    timestamptz_type timestamp with time zone,
    uuid_type uuid,
    array_int_type integer[]
);

ALTER TABLE IF EXISTS testing.base_types
    OWNER to mag_owner;
