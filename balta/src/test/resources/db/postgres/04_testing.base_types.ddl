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
