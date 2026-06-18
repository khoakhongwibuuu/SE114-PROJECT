import urllib.request
import json
import sys

# use utf-8 for print
sys.stdout.reconfigure(encoding='utf-8')

base_url = "http://localhost:8080/api/v1"

def login(email, password):
    data = json.dumps({"email": email, "password": password}).encode('utf-8')
    req = urllib.request.Request(f"{base_url}/auth/login", data=data, headers={'Content-Type': 'application/json'})
    try:
        with urllib.request.urlopen(req) as response:
            res = json.loads(response.read().decode('utf-8'))
            if 'data' in res and 'accessToken' in res['data']:
                return res['data']['accessToken']
            elif 'accessToken' in res:
                return res['accessToken']
            elif 'token' in res:
                return res['token']
            return None
    except Exception as e:
        print(f"Login failed for {email}: {e}")
        return None

def get(endpoint, token):
    req = urllib.request.Request(f"{base_url}{endpoint}", headers={'Authorization': f'Bearer {token}'})
    try:
        with urllib.request.urlopen(req) as response:
            return json.loads(response.read().decode('utf-8'))
    except Exception as e:
        print(f"GET {endpoint} failed: {e}")
        return None

# Checkpoint 1
print("\n--- CP1: Family Switching (kiet@gmail.com) ---")
token_kiet = login("kiet@gmail.com", "Kiet13012006")
if token_kiet:
    families = get("/families/my-list", token_kiet)
    if families and 'data' in families:
        print("Families count:", len(families['data']))
    else:
        print("Families: None")

# Checkpoint 2
print("\n--- CP2: Community Groups (kiet@gmail.com) ---")
if token_kiet:
    groups = get("/communities/my", token_kiet)
    if groups and 'data' in groups:
        print("My Groups count:", len(groups['data']))
    else:
        print("My Groups: None")

# Checkpoint 3
print("\n--- CP3: Booking Center (kiet@gmail.com & bacsinhikhoa@gmail.com) ---")
if token_kiet:
    bookings = get("/bookings/patient", token_kiet)
    if bookings and 'data' in bookings:
        print("Patient Bookings count:", len(bookings['data']))
        for b in bookings['data']:
            print(f"  Booking {b.get('id')}: status={b.get('status')}")
    else:
        print("Patient Bookings: None")

token_doctor = login("bacsinhikhoa@gmail.com", "Bacsinhikhoa")
doc_bookings_data = []
if token_doctor:
    doc_bookings = get("/bookings/doctor", token_doctor)
    if doc_bookings and 'data' in doc_bookings:
        print("Doctor Bookings count:", len(doc_bookings['data']))
        doc_bookings_data = doc_bookings['data']
        for b in doc_bookings_data:
            print(f"  Doc Booking {b.get('id')}: status={b.get('status')}")
    else:
        print("Doctor Bookings: None")

# Checkpoint 4
print("\n--- CP4: Consultation History ---")
if token_doctor:
    inbox = get("/bookings/consultation-inbox", token_doctor)
    if inbox and 'data' in inbox:
        threads = inbox['data']
        print("Consultation threads count:", len(threads))
        for t in threads:
            print(f"Thread {t.get('id')}: booking_id={t.get('bookingId')} status={t.get('status')} CTA='{t.get('ctaText', '')}'")
    else:
        print("Consultation threads: None")
