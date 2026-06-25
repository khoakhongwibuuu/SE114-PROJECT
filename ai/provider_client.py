import json
import time
import urllib.error
import urllib.request
from typing import Any

from config import settings


class AiProviderError(RuntimeError):
    pass


def _strip_unsupported_schema_keys(schema: dict[str, Any]) -> dict[str, Any]:
    if not isinstance(schema, dict):
        return schema
    cleaned = {}
    for k, v in schema.items():
        if k == "additionalProperties":
            continue
        if isinstance(v, dict):
            cleaned[k] = _strip_unsupported_schema_keys(v)
        elif isinstance(v, list):
            cleaned[k] = [_strip_unsupported_schema_keys(i) if isinstance(i, dict) else i for i in v]
        else:
            cleaned[k] = v
    return cleaned


def call_structured_response(
    *,
    model: str,
    instructions: str,
    user_input: str,
    schema_name: str,
    schema: dict[str, Any],
) -> dict[str, Any]:
    if not settings.has_api_key:
        raise AiProviderError("AI_API_KEY is missing.")

    if settings.ai_provider == "gemini":
        gemini_schema = _strip_unsupported_schema_keys(schema)
        payload = {
            "contents": [{"role": "user", "parts": [{"text": user_input}]}],
            "systemInstruction": {"parts": [{"text": instructions}]},
            "generationConfig": {
                "responseMimeType": "application/json",
                "responseSchema": gemini_schema
            }
        }
    elif settings.ai_provider == "openai":
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
    else:
        raise AiProviderError(f"Unsupported AI_PROVIDER: {settings.ai_provider}")

    last_error: Exception | None = None
    attempts = max(1, settings.ai_max_retries + 1)
    for attempt in range(attempts):
        try:
            if settings.ai_provider == "gemini":
                return _post_gemini(payload, model)
            else:
                return _post_json(payload)
        except AiProviderError as error:
            last_error = error
            if attempt < attempts - 1:
                time.sleep(0.4 * (attempt + 1))

    raise AiProviderError(str(last_error) if last_error else "AI provider request failed.")


def _post_gemini(payload: dict[str, Any], model: str) -> dict[str, Any]:
    base_url = settings.ai_base_url.rstrip("/")
    url = f"{base_url}/{model}:generateContent?key={settings.ai_api_key}"
    
    request = urllib.request.Request(
        url,
        data=json.dumps(payload).encode("utf-8"),
        headers={
            "Content-Type": "application/json",
        },
        method="POST",
    )

    try:
        with urllib.request.urlopen(request, timeout=settings.ai_timeout_ms / 1000) as response:
            body = response.read().decode("utf-8")
    except urllib.error.HTTPError as error:
        detail = error.read().decode("utf-8", errors="replace")
        raise AiProviderError(f"Gemini API HTTP {error.code}: {detail[:500]}") from error
    except urllib.error.URLError as error:
        raise AiProviderError(f"Gemini API connection failed: {error.reason}") from error
    except TimeoutError as error:
        raise AiProviderError("Gemini API request timed out.") from error

    try:
        data = json.loads(body)
    except json.JSONDecodeError as error:
        raise AiProviderError("Gemini API returned invalid JSON.") from error

    try:
        output_text = data.get("candidates", [])[0].get("content", {}).get("parts", [])[0].get("text", "")
    except IndexError:
        raise AiProviderError("Gemini API response did not include valid output text.")

    if not output_text.strip():
        raise AiProviderError("Gemini API response output text was empty.")

    try:
        parsed = json.loads(output_text)
    except json.JSONDecodeError as error:
        raise AiProviderError("Gemini API output did not match JSON contract.") from error

    if not isinstance(parsed, dict):
        raise AiProviderError("Gemini API output root must be a JSON object.")
    return parsed


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
