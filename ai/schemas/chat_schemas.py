from pydantic import BaseModel
from typing import Optional, Any, Dict

class ChatRequest(BaseModel):
    message: str
    conversationId: Optional[int] = None
    profileId: Optional[int] = None

class ChatReply(BaseModel):
    reply: str
    id: int
    message_id: Optional[int] = None
    conversation_id: Optional[int] = None
    sqlGenerated: Optional[str] = None
    data: Optional[Any] = None

class TTSRequest(BaseModel):
    text: str
    lang: str = "vi"

class TTSResponseData(BaseModel):
    audio_base64: str

class TTSResponse(BaseModel):
    data: TTSResponseData
