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

DROP TABLE IF EXISTS testing.strange_columns;

CREATE TABLE testing.strange_columns
(
    id_strange_columns  BIGINT NOT NULL,

    col1                TEXT NOT NULL,
    "Col1"              TEXT NOT NULL,
    "col 1"             TEXT NOT NULL,
    "col-1"             TEXT NOT NULL,
    "colá"              TEXT NOT NULL,
    "1col"              TEXT NOT NULL,
    PRIMARY KEY (id_strange_columns)
);

ALTER TABLE IF EXISTS testing.strange_columns
    OWNER to mag_owner;
