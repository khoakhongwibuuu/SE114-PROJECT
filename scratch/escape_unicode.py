import re

str_pat = re.compile(r'"([^"\\]*(?:\\.[^"\\]*)*)"')

def escape_non_ascii(match):
    s = match.group(0)
    # Check if there are non-ASCII characters
    if any(ord(c) > 127 for c in s):
        escaped = []
        for c in s:
            if ord(c) > 127:
                escaped.append(f'\\u{ord(c):04x}')
            else:
                escaped.append(c)
        return ''.join(escaped)
    return s

files = [
    'frontend/app/src/main/java/com/example/carenest/feature/social/presentation/SocialFeedScreen.kt',
    'frontend/app/src/main/java/com/example/carenest/feature/social/presentation/PostDetailScreen.kt',
    'frontend/app/src/main/java/com/example/carenest/feature/social/presentation/PostDetailViewModel.kt'
]

for path in files:
    with open(path, 'r', encoding='utf-8', errors='replace') as f:
        content = f.read()
    
    # We use re.sub with a callback function to replace string literals
    new_content = str_pat.sub(escape_non_ascii, content)
    
    with open(path, 'w', encoding='utf-8') as f:
        f.write(new_content)
    print(f'Escaped non-ASCII in {path}')
