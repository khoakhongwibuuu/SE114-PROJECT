from typing import Any, Literal, Optional

from pydantic import BaseModel, Field


class ChatRequest(BaseModel):
    message: str
    conversationId: Optional[int] = None
    profileId: Optional[int] = None


class AiAction(BaseModel):
    type: Literal["book_doctor", "go_emergency", "track_symptom", "ask_family"]
    label: str


class StructuredChatSafety(BaseModel):
    needs_doctor: bool
    needs_emergency: bool
    disclaimer: str


class StructuredChatPayload(BaseModel):
    intent: Literal["medication_guidance", "symptom_triage", "general_health", "unsupported"]
    summary: str
    advice: list[str] = Field(default_factory=list)
    risk_level: Literal["low", "medium", "high", "emergency"]
    follow_up_questions: list[str] = Field(default_factory=list)
    recommended_actions: list[AiAction] = Field(default_factory=list)
    safety: StructuredChatSafety


class ChatReply(BaseModel):
    reply: str
    id: int
    message_id: Optional[int] = None
    conversation_id: Optional[int] = None
    sqlGenerated: Optional[str] = None
    data: Optional[Any] = None
    structured: Optional[StructuredChatPayload] = None


class TTSRequest(BaseModel):
    text: str
    lang: str = "vi"


class TTSResponseData(BaseModel):
    audio_base64: str


class TTSResponse(BaseModel):
    data: TTSResponseData
