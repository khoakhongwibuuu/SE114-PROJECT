-- Reset local QA database by recreating the public schema.
DROP SCHEMA IF EXISTS public CASCADE;
CREATE SCHEMA public;
GRANT ALL ON SCHEMA public TO carenest_user;
GRANT ALL ON SCHEMA public TO public;
