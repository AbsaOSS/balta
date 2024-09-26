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

CREATE OR REPLACE FUNCTION testing.simple_function(
    IN i_return_data    BOOLEAN,
    OUT int_data        INTEGER,
    OUT text_data       TEXT,
    OUT timestamp_data  TIMESTAMP WITH TIME ZONE,
    OUT uuid_data       UUID,
    OUT ignored_data    TEXT
) RETURNS record AS
$$
-------------------------------------------------------------------------------
--
-- Function: testing.simple_function(1)
--      Function returning static data for testing purposes
--
-- Parameters:
--      i_return_data   - flag if data are to be returned or not         -
--
-- Returns:
--      sample data
--
-------------------------------------------------------------------------------
DECLARE
BEGIN
    ignored_data := 'This is to be ignored';

    IF i_return_data THEN
        int_data := 42;
        text_data := 'Hello World!';
        timestamp_data := '2023-01-01 00:00:00+00'::TIMESTAMP WITH TIME ZONE;
        uuid_data := 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'::UUID;
    END IF;

    RETURN;
END;
$$
    LANGUAGE plpgsql VOLATILE
                     SECURITY DEFINER;

GRANT EXECUTE ON FUNCTION testing.simple_function(BOOLEAN) TO mag_owner;
