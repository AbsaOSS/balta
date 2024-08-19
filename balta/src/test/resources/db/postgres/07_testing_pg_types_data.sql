TRUNCATE testing.pg_types;


INSERT INTO testing.pg_types(
    id, json_type, jsonb_type, array_of_json_type)
VALUES (
           1,
           '{"a": 1, "b": "Hello"}'::JSON,
           '{"Hello"    :     "World"}'::JSONB,
           array['{"a": 2, "body": "Hold the line!"}', '{"a": 3, "body": ""}', '{"a": 4}']::json[]
       );


INSERT INTO testing.pg_types(id)
VALUES (2);
