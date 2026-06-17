from typing import Literal, Optional

from pydantic import BaseModel, Field


class OcrMedicationSafety(BaseModel):
    requires_confirmation: bool = True
    can_save_directly: bool = False
    disclaimer: str


class OcrMedicationItem(BaseModel):
    name: str
    strength: Optional[str] = None
    form: Literal["tablet", "capsule", "syrup", "drop", "unknown"]
    dose_instruction: Optional[str] = None
    frequency: Optional[str] = None
    duration_days: Optional[int] = Field(default=None, ge=0)
    route: Literal["oral", "topical", "inhaled", "unknown"]
    confidence: float = Field(default=0.0, ge=0.0, le=1.0)
    warnings: list[str] = Field(default_factory=list)


class OcrMedicationPayload(BaseModel):
    schema_version: Literal["ocr.medication.v1"] = "ocr.medication.v1"
    document_type: Literal["medicine_label", "prescription", "unknown"]
    confidence: float = Field(ge=0.0, le=1.0)
    medications: list[OcrMedicationItem] = Field(default_factory=list)
    warnings: list[str] = Field(default_factory=list)
    raw_text: str
    safety: OcrMedicationSafety
