import os
import sys

if sys.version_info >= (3, 7):
    sys.stdout.reconfigure(encoding='utf-8')

search_terms = ["activeTab"]
search_dir = r"d:\DoAn_MB1\CareNest\frontend\app\src\main\java\com\example\carenest\feature\community\presentation"

for root, dirs, files in os.walk(search_dir):
    for file in files:
        if file.endswith("ViewModel.kt"):
            path = os.path.join(root, file)
            try:
                with open(path, 'r', encoding='utf-8') as f:
                    lines = f.readlines()
                    for i, line in enumerate(lines):
                        for term in search_terms:
                            if term in line:
                                print(f"{path}:{i+1}:{line.strip()}")
            except UnicodeDecodeError:
                pass
