import psycopg2

DSN = 'postgresql://postgres:doletuankiet06@db.przewpjbkomkwgzyfadc.supabase.co:5432/postgres'
try:
    conn = psycopg2.connect(DSN)
    conn.autocommit = True
    cursor = conn.cursor()
    
    # Check if pg_cron is available
    cursor.execute("SELECT 1 FROM pg_extension WHERE extname = 'pg_cron';")
    has_cron = cursor.fetchone()
    if has_cron:
        print('pg_cron is enabled.')
        # Try to use pg_cron to clean up notifications older than 7 days
        cursor.execute("""
        SELECT cron.schedule(
            'cleanup-old-notifications', 
            '0 0 * * *', 
            $$DELETE FROM notifications WHERE created_at < NOW() - INTERVAL '7 days'$$
        );
        """)
        print('Created pg_cron job for notification cleanup.')
    else:
        print('pg_cron not found.')
        
    print('Connection successful.')
    conn.close()
except Exception as e:
    print('Error:', e)
