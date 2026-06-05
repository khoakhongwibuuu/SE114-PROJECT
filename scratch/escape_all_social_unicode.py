import os
import re

str_pat = re.compile(r'"([^"\\]*(?:\\.[^"\\]*)*)"')

def escape_non_ascii(match):
    s = match.group(0)
    if any(ord(c) > 127 for c in s):
        escaped = []
        for c in s:
            if ord(c) > 127:
                escaped.append(f'\\u{ord(c):04x}')
            else:
                escaped.append(c)
        return ''.join(escaped)
    return s

social_dir = 'frontend/app/src/main/java/com/example/carenest/feature/social'

for root, dirs, files in os.walk(social_dir):
    for file in files:
        if file.endswith('.kt'):
            path = os.path.join(root, file)
            with open(path, 'r', encoding='utf-8', errors='replace') as f:
                content = f.read()
            
            new_content = str_pat.sub(escape_non_ascii, content)
            
            if content != new_content:
                with open(path, 'w', encoding='utf-8') as f:
                    f.write(new_content)
                print(f'Escaped non-ASCII in {path}')
print('Unicode escaping process completed.')
