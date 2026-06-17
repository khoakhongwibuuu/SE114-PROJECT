from typing import Literal

from pydantic import BaseModel, Field


class OcrMedicationItem(BaseModel):
    name: str
    strength: str
    form: Literal["tablet", "capsule", "syrup", "drop", "unknown"]
    dose_instruction: str
    frequency: str
    duration_days: int = Field(ge=0)
    route: Literal["oral", "topical", "inhaled", "unknown"]


class OcrMedicationPayload(BaseModel):
    document_type: Literal["medicine_label", "prescription", "unknown"]
    confidence: float = Field(ge=0.0, le=1.0)
    medications: list[OcrMedicationItem] = Field(default_factory=list)
    warnings: list[str] = Field(default_factory=list)
    raw_text: str


class OcrVaccinationEntry(BaseModel):
    vaccine_name: str
    dose_number: int = Field(ge=1)
    date_administered: str
    facility: str


class OcrVaccinationPayload(BaseModel):
    document_type: Literal["vaccination_record", "unknown"]
    confidence: float = Field(ge=0.0, le=1.0)
    entries: list[OcrVaccinationEntry] = Field(default_factory=list)
    warnings: list[str] = Field(default_factory=list)
    raw_text: str
