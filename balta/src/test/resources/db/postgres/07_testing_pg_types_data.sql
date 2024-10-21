/*
 * Copyright 2023 ABSA Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
