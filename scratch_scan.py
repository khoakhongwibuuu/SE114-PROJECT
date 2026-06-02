import os
import re

target_dir = r"d:\DoAn_MB1\CareNest\frontend\app\src\main\java\com\example\carenest"
results = []

# Pattern matching words with '?' in the middle, like th?p, c?a, d?ch, or containing '\uFFFD'
# We also include typical mojibake characters like 
pattern = re.compile(r'\b[a-zA-Z\u00C0-\u1EF9]*\?[a-zA-Z\u00C0-\u1EF9]+\b|[\uFFFD]')

for root, dirs, files in os.walk(target_dir):
    for file in files:
        if file.endswith(".kt"):
            filepath = os.path.join(root, file)
            try:
                with open(filepath, "r", encoding="utf-8", errors="replace") as f:
                    content = f.read()
                
                lines = content.splitlines()
                for idx, line in enumerate(lines, 1):
                    # Find matches
                    matches = pattern.findall(line)
                    if matches:
                        # Exclude comments if possible, but keeping it simple first.
                        # Also check if it looks like a URL parameter like ?id= or ?ref=, or kotlin syntax like ?.
                        # E.g. "something?.let" or "?:" or "val a: String? = null"
                        # To filter: a match must not be part of ?. or ?: or ?= or similar operators.
                        filtered_matches = []
                        for m in matches:
                            if m == '\uFFFD':
                                filtered_matches.append(m)
                            else:
                                # Check if it's a real word with a question mark in it (like th?p)
                                # and not Kotlin's nullable type or safe call operator.
                                # Let's make sure it doesn't match standard code pattern like:
                                # "Int?", "String?", "User?", "?."
                                filtered_matches.append(m)
                        
                        if filtered_matches:
                            results.append({
                                "file": filepath,
                                "line_num": idx,
                                "matches": filtered_matches,
                                "content": line.strip()
                            })
            except Exception as e:
                pass

output_path = r"d:\DoAn_MB1\CareNest\mojibake_scan_results.txt"
with open(output_path, "w", encoding="utf-8") as out:
    out.write(f"Found {len(results)} potential mojibake lines.\n")
    for res in results:
        out.write(f"{res['file']}:{res['line_num']} - Matches: {res['matches']} - Content: {res['content']}\n")

print(f"Scan completed. Found {len(results)} potential lines. Results in mojibake_scan_results.txt.")
