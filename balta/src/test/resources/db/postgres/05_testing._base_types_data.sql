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

TRUNCATE testing.base_types;

INSERT INTO testing.base_types(
    long_type, boolean_type, char_type, string_type, int_type, double_type, float_type, bigdecimal_type, date_type,
    time_type, timestamp_type, timestamptz_type,
    uuid_type, array_int_type)
VALUES (1, true, 'a', 'hello world', 257, 3.14, 2.71, 123456789.0123456789, '2022-08-09'::date,
        '10:12:15'::time, '2020-06-02 01:00:00'::timestamp without time zone, '2021-04-03 11:00:00 CET'::timestamp with time zone,
        '090416f8-7da0-4598-844b-63659334e5b6'::UUID, '{1,2,3}'::INTEGER[]);

INSERT INTO testing.base_types(
    long_type)
VALUES (NULL);
