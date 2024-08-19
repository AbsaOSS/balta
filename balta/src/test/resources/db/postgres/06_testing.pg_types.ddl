CREATE TABLE testing.pg_types
(
    id bigint NOT NULL,
    json_type json,
    jsonb_type jsonb,
    array_of_json_type json[],
    PRIMARY KEY (id)
);

ALTER TABLE IF EXISTS testing.pg_types
    OWNER to mag_owner;
