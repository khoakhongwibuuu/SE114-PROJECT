import json
import os

log_file = r"C:\Users\Admin\.gemini\antigravity-ide\brain\630a149e-e44b-42d6-89a7-6a991c6bf2ab\.system_generated\logs\transcript.jsonl"

files_to_restore = {}

with open(log_file, "r", encoding="utf-8") as f:
    for line in f:
        try:
            data = json.loads(line)
            if data.get("type") == "PLANNER_RESPONSE":
                tool_calls = data.get("tool_calls", [])
                for tc in tool_calls:
                    if tc.get("name") == "write_to_file":
                        args = tc.get("args", {})
                        target = args.get("TargetFile", "")
                        content = args.get("CodeContent", "")
                        
                        # Remove surrounding quotes if they exist from JSON serialization
                        if target.startswith('"') and target.endswith('"'):
                            target = target[1:-1]
                        
                        if content.startswith('"') and content.endswith('"'):
                            content = content[1:-1]
                            # Unescape newlines
                            content = content.replace("\\n", "\n").replace("\\\"", "\"").replace("\\r", "\r")
                            
                        # We want to restore any file in feature/medical
                        if "feature\\\\medical" in target or "feature/medical" in target:
                            # Normalize path
                            target = target.replace("\\\\", "\\")
                            files_to_restore[target] = content
        except Exception as e:
            pass

for target, content in files_to_restore.items():
    print(f"Restoring {target}...")
    os.makedirs(os.path.dirname(target), exist_ok=True)
    with open(target, "w", encoding="utf-8") as out:
        out.write(content)

print(f"Restored {len(files_to_restore)} files.")
