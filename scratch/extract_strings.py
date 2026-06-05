import re

str_pat = re.compile(r'"([^"\\]*(?:\\.[^"\\]*)*)"')

files = [
    'frontend/app/src/main/java/com/example/carenest/feature/social/presentation/SocialFeedScreen.kt',
    'frontend/app/src/main/java/com/example/carenest/feature/social/presentation/PostDetailScreen.kt',
    'frontend/app/src/main/java/com/example/carenest/feature/social/presentation/PostDetailViewModel.kt'
]

with open('scratch/strings_output.txt', 'w', encoding='utf-8') as out:
    for path in files:
        out.write(f'=== {path}\n')
        with open(path, 'r', encoding='utf-8', errors='replace') as f:
            for i, line in enumerate(f, 1):
                matches = str_pat.findall(line)
                for m in matches:
                    if any(ord(c) > 127 for c in m):
                        out.write(f'{i}: {m}\n')
