import os
import re

str_pat = re.compile(r'"([^"\\]*(?:\\.[^"\\]*)*)"')

social_dir = 'frontend/app/src/main/java/com/example/carenest/feature/social'
output_file = 'scratch/all_social_strings.txt'

with open(output_file, 'w', encoding='utf-8') as out:
    for root, dirs, files in os.walk(social_dir):
        for file in files:
            if file.endswith('.kt'):
                path = os.path.join(root, file)
                rel_path = os.path.relpath(path, 'frontend/app/src/main/java/com/example/carenest')
                
                # Check for non-ASCII characters in string literals
                with open(path, 'r', encoding='utf-8', errors='replace') as f:
                    file_has_non_ascii = False
                    file_lines = []
                    for i, line in enumerate(f, 1):
                        matches = str_pat.findall(line)
                        for m in matches:
                            if any(ord(c) > 127 for c in m):
                                file_lines.append((i, m))
                                file_has_non_ascii = True
                    
                    if file_has_non_ascii:
                        out.write(f'=== {rel_path}\n')
                        for i, m in file_lines:
                            out.write(f'{i}: {m}\n')
print(f'Done scanning. Results written to {output_file}')
