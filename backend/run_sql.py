import psycopg2
import os
import sys
from urllib.parse import urlparse

def main():
    conn_string = os.environ.get("CARENEST_DATABASE_DSN")
    if not conn_string:
        print("Error: CARENEST_DATABASE_DSN is required.")
        sys.exit(1)

    parsed = urlparse(conn_string)
    print(f"Connecting to database host={parsed.hostname} db={parsed.path.lstrip('/')} user={parsed.username}...")
    try:
        conn = psycopg2.connect(conn_string)
        conn.autocommit = True
        cursor = conn.cursor()
        print("Connected successfully!")
        
        with open('dump.sql', 'r', encoding='utf-8') as f:
            sql = f.read()
            
        print(f"Read {len(sql)} characters from dump.sql. Executing...")
        cursor.execute(sql)
        print("Execution successful!")
        
    except Exception as e:
        print(f"Error: {e}")
        sys.exit(1)
    finally:
        if 'conn' in locals():
            conn.close()

if __name__ == "__main__":
    main()
