import json
import time
import urllib.error
import urllib.request
from typing import Any

from config import settings


class AiProviderError(RuntimeError):
    pass


def call_structured_response(
    *,
    model: str,
    instructions: str,
    user_input: str,
    schema_name: str,
    schema: dict[str, Any],
) -> dict[str, Any]:
    if settings.ai_provider != "openai":
        raise AiProviderError(f"Unsupported AI_PROVIDER: {settings.ai_provider}")
    if not settings.has_api_key:
        raise AiProviderError("AI_API_KEY is missing.")

    payload = {
        "model": model,
        "instructions": instructions,
        "input": user_input,
        "text": {
            "format": {
                "type": "json_schema",
                "name": schema_name,
                "schema": schema,
                "strict": True,
            }
        },
    }

    last_error: Exception | None = None
    attempts = max(1, settings.ai_max_retries + 1)
    for attempt in range(attempts):
        try:
            return _post_json(payload)
        except AiProviderError as error:
            last_error = error
            if attempt < attempts - 1:
                time.sleep(0.4 * (attempt + 1))

    raise AiProviderError(str(last_error) if last_error else "AI provider request failed.")


def _post_json(payload: dict[str, Any]) -> dict[str, Any]:
    request = urllib.request.Request(
        settings.ai_base_url,
        data=json.dumps(payload).encode("utf-8"),
        headers={
            "Authorization": f"Bearer {settings.ai_api_key}",
            "Content-Type": "application/json",
        },
        method="POST",
    )

    try:
        with urllib.request.urlopen(request, timeout=settings.ai_timeout_ms / 1000) as response:
            body = response.read().decode("utf-8")
    except urllib.error.HTTPError as error:
        detail = error.read().decode("utf-8", errors="replace")
        raise AiProviderError(f"AI provider HTTP {error.code}: {detail[:500]}") from error
    except urllib.error.URLError as error:
        raise AiProviderError(f"AI provider connection failed: {error.reason}") from error
    except TimeoutError as error:
        raise AiProviderError("AI provider request timed out.") from error

    try:
        data = json.loads(body)
    except json.JSONDecodeError as error:
        raise AiProviderError("AI provider returned invalid JSON.") from error

    output_text = _extract_output_text(data)
    try:
        parsed = json.loads(output_text)
    except json.JSONDecodeError as error:
        raise AiProviderError("AI provider output did not match JSON contract.") from error

    if not isinstance(parsed, dict):
        raise AiProviderError("AI provider output root must be a JSON object.")
    return parsed


def _extract_output_text(data: dict[str, Any]) -> str:
    direct = data.get("output_text")
    if isinstance(direct, str) and direct.strip():
        return direct

    for item in data.get("output", []):
        if not isinstance(item, dict):
            continue
        for content in item.get("content", []):
            if not isinstance(content, dict):
                continue
            text = content.get("text")
            if isinstance(text, str) and text.strip():
                return text

    raise AiProviderError("AI provider response did not include output text.")
