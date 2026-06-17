from fastapi import APIRouter
from pydantic import BaseModel

from config import settings
from schemas.ocr_schemas import OcrMedicationPayload, OcrVaccinationPayload

router = APIRouter()


class RawTextRequest(BaseModel):
    raw_text: str


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
        warnings=warnings or ["OCR provider integration will be added in a later batch."],
        raw_text=request.raw_text,
    )


@router.post("/vaccination/parse", response_model=OcrVaccinationPayload)
async def parse_vaccination_text(request: RawTextRequest):
    warnings = []
    if not settings.ocr_enabled:
        warnings.append("OCR feature flag is disabled.")
    if not settings.has_api_key:
        warnings.append("AI_API_KEY is missing. Provider-backed OCR is unavailable.")

    return OcrVaccinationPayload(
        document_type="unknown",
        confidence=0.0,
        entries=[],
        warnings=warnings or ["OCR provider integration will be added in a later batch."],
        raw_text=request.raw_text,
    )
