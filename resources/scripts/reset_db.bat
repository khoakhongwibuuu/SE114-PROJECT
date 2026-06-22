@echo off
echo =======================================================
echo CareNest - Resetting PostgreSQL Database (Docker)
echo =======================================================
docker exec -i carenest-postgres psql -U carenest_user -d carenest_db -c "DROP SCHEMA IF EXISTS public CASCADE; CREATE SCHEMA public; GRANT ALL ON SCHEMA public TO carenest_user; GRANT ALL ON SCHEMA public TO public;"
if %ERRORLEVEL% equ 0 (
    echo Database reset successfully!
) else (
    echo Database reset failed. Please make sure the docker container carenest-postgres is running.
)
