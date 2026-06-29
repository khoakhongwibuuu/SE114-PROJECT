from fastapi import APIRouter, HTTPException
from pydantic import BaseModel, Field

from config import settings
from provider_client import AiProviderError, call_structured_response
from schemas.ocr_schemas import OcrMedicationPayload, OcrMedicationSafety

router = APIRouter()


OCR_MEDICINE_JSON_SCHEMA = {
    "type": "object",
    "additionalProperties": False,
    "required": ["schema_version", "document_type", "confidence", "medications", "warnings", "raw_text", "safety"],
    "properties": {
        "schema_version": {"type": "string", "enum": ["ocr.medication.v1"]},
        "document_type": {"type": "string", "enum": ["medicine_label", "prescription", "unknown"]},
        "confidence": {"type": "number", "minimum": 0.0, "maximum": 1.0},
        "medications": {
            "type": "array",
            "items": {
                "type": "object",
                "additionalProperties": False,
                "required": [
                    "name",
                    "strength",
                    "form",
                    "dose_instruction",
                    "frequency",
                    "duration_days",
                    "route",
                    "confidence",
                    "warnings",
                ],
                "properties": {
                    "name": {"type": "string"},
                    "strength": {"anyOf": [{"type": "string"}, {"type": "null"}]},
                    "form": {"type": "string", "enum": ["tablet", "capsule", "syrup", "drop", "unknown"]},
                    "dose_instruction": {"anyOf": [{"type": "string"}, {"type": "null"}]},
                    "frequency": {"anyOf": [{"type": "string"}, {"type": "null"}]},
                    "duration_days": {"anyOf": [{"type": "integer", "minimum": 0}, {"type": "null"}]},
                    "route": {"type": "string", "enum": ["oral", "topical", "inhaled", "unknown"]},
                    "confidence": {"type": "number", "minimum": 0.0, "maximum": 1.0},
                    "warnings": {"type": "array", "items": {"type": "string"}},
                },
            },
        },
        "warnings": {"type": "array", "items": {"type": "string"}},
        "raw_text": {"type": "string"},
        "safety": {
            "type": "object",
            "additionalProperties": False,
            "required": ["requires_confirmation", "can_save_directly", "disclaimer"],
            "properties": {
                "requires_confirmation": {"type": "boolean"},
                "can_save_directly": {"type": "boolean"},
                "disclaimer": {"type": "string"},
            },
        },
    },
}


OCR_MEDICINE_INSTRUCTIONS = """
Extract medicine information from OCR text for CareNest.
Return only JSON matching the supplied schema.
Strictly ignore clinic names, doctor names, patient names, addresses, phone numbers, and any other non-medication text. Focus ONLY on the actual prescribed medicines to avoid returning junk data.
Do not infer missing strength, dose, route, or duration. Use null or unknown when unclear.
Never return vaccination entries. This endpoint is only for medicine labels and prescriptions.
Set safety.requires_confirmation=true and safety.can_save_directly=false.
Add warnings for uncertain, incomplete, or unsafe medication details.
""".strip()


class RawTextRequest(BaseModel):
    raw_text: str = Field(min_length=1)


@router.post("/medicine/parse", response_model=OcrMedicationPayload)
async def parse_medicine_text(request: RawTextRequest):
    if settings.ocr_enabled and settings.has_api_key:
        try:
            provider_payload = call_structured_response(
                model=settings.ai_model_ocr,
                instructions=OCR_MEDICINE_INSTRUCTIONS,
                user_input=request.raw_text,
                schema_name="carenest_ocr_medication_v1",
                schema=OCR_MEDICINE_JSON_SCHEMA,
            )
            payload = OcrMedicationPayload(**provider_payload)
            payload.safety.requires_confirmation = True
            payload.safety.can_save_directly = False
            return payload
        except AiProviderError as error:
            raise HTTPException(status_code=502, detail=str(error)) from error

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
