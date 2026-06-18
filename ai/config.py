import os
from dataclasses import dataclass


def _as_bool(value: str | None, default: bool = False) -> bool:
    if value is None:
        return default
    return value.strip().lower() in {"1", "true", "yes", "on"}


def _as_int(value: str | None, default: int) -> int:
    try:
        return int(value) if value is not None and value.strip() else default
    except ValueError:
        return default


@dataclass(frozen=True)
class Settings:
    ai_enabled: bool
    ocr_enabled: bool
    ai_provider: str
    ai_api_key: str
    ai_base_url: str
    ai_model_chat: str
    ai_model_ocr: str
    ai_timeout_ms: int
    ai_max_retries: int

    @property
    def has_api_key(self) -> bool:
        return bool(self.ai_api_key.strip())


def load_settings() -> Settings:
    return Settings(
        ai_enabled=_as_bool(os.getenv("AI_ENABLED"), False),
        ocr_enabled=_as_bool(os.getenv("OCR_ENABLED"), False),
        ai_provider=os.getenv("AI_PROVIDER", "openai").strip() or "openai",
        ai_api_key=os.getenv("AI_API_KEY", ""),
        ai_base_url=os.getenv("AI_BASE_URL", "https://api.openai.com/v1/responses").strip()
        or "https://api.openai.com/v1/responses",
        ai_model_chat=os.getenv("AI_MODEL_CHAT", "gpt-5.2").strip() or "gpt-5.2",
        ai_model_ocr=os.getenv("AI_MODEL_OCR", "gpt-5.2").strip() or "gpt-5.2",
        ai_timeout_ms=_as_int(os.getenv("AI_TIMEOUT_MS"), 15000),
        ai_max_retries=_as_int(os.getenv("AI_MAX_RETRIES"), 2),
    )


settings = load_settings()
