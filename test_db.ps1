docker compose up -d db ; Start-Sleep -Seconds 5 ; docker compose exec db psql -U carenest_user -d carenest_db -c "SELECT 1;"
