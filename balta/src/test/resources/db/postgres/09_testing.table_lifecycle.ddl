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

DROP TABLE IF EXISTS testing.table_lifecycle;

CREATE TABLE testing.table_lifecycle
(
    id_field bigint NOT NULL,
    text_field text,
    boolean_field boolean,
    created_at timestamp with time zone NOT NULL DEFAULT now(),
    PRIMARY KEY (id_field)
);

ALTER TABLE IF EXISTS testing.table_lifecycle
    OWNER to mag_owner;
