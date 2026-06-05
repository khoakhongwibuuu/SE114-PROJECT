import psycopg2
import sys

def main():
    conn_string = "postgresql://postgres:doletuankiet06@db.przewpjbkomkwgzyfadc.supabase.co:5432/postgres"
    print(f"Connecting to {conn_string}...")
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
