import codecs

path = 'frontend/app/src/main/java/com/example/carenest/feature/medical/presentation/VaccineScheduleViewModel.kt'
with codecs.open(path, 'r', encoding='utf-8') as f:
    content = f.read()

if content.startswith('"'):
    if content.endswith('"'):
        content = content[1:-1]
    else:
        content = content[1:]
    
    parsed = content.replace('\\n', '\n').replace('\\"', '"')
    with codecs.open(path, 'w', encoding='utf-8') as f:
        f.write(parsed)
    print("Fixed parsing string")
else:
    print("No quotes found")
