from fastapi import APIRouter
from pydantic import BaseModel, Field

from config import settings
from schemas.ocr_schemas import OcrMedicationPayload, OcrMedicationSafety

router = APIRouter()


class RawTextRequest(BaseModel):
    raw_text: str = Field(min_length=1)


@router.post("/medicine/parse", response_model=OcrMedicationPayload)
async def parse_medicine_text(request: RawTextRequest):
    warnings = []
    if not settings.ocr_enabled:
        warnings.append("OCR feature flag is disabled.")
    if not settings.has_api_key:
        warnings.append("AI_API_KEY is missing. Provider-backed OCR is unavailable.")

    return OcrMedicationPayload(
        document_type="unknown",
        confidence=0.0,
        medications=[],
        warnings=warnings or ["OCR medicine provider integration is ready for the final AI/OCR batch."],
        raw_text=request.raw_text,
        safety=OcrMedicationSafety(
            requires_confirmation=True,
            can_save_directly=False,
            disclaimer="OCR medicine output must be reviewed and confirmed by the user before saving.",
        ),
    )
