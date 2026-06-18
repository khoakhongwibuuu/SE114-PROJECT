import random

from fastapi import APIRouter, HTTPException

from config import settings
from provider_client import AiProviderError, call_structured_response
from schemas.chat_schemas import (
    AiAction,
    ChatReply,
    ChatRequest,
    StructuredChatPayload,
    StructuredChatSafety,
)

router = APIRouter()


CHAT_JSON_SCHEMA = {
    "type": "object",
    "additionalProperties": False,
    "required": [
        "intent",
        "summary",
        "advice",
        "risk_level",
        "follow_up_questions",
        "recommended_actions",
        "safety",
    ],
    "properties": {
        "intent": {
            "type": "string",
            "enum": ["medication_guidance", "symptom_triage", "general_health", "unsupported"],
        },
        "summary": {"type": "string"},
        "advice": {"type": "array", "items": {"type": "string"}},
        "risk_level": {"type": "string", "enum": ["low", "medium", "high", "emergency"]},
        "follow_up_questions": {"type": "array", "items": {"type": "string"}},
        "recommended_actions": {
            "type": "array",
            "items": {
                "type": "object",
                "additionalProperties": False,
                "required": ["type", "label"],
                "properties": {
                    "type": {
                        "type": "string",
                        "enum": ["book_doctor", "go_emergency", "track_symptom", "ask_family"],
                    },
                    "label": {"type": "string"},
                },
            },
        },
        "safety": {
            "type": "object",
            "additionalProperties": False,
            "required": ["needs_doctor", "needs_emergency", "disclaimer"],
            "properties": {
                "needs_doctor": {"type": "boolean"},
                "needs_emergency": {"type": "boolean"},
                "disclaimer": {"type": "string"},
            },
        },
    },
}


CHAT_INSTRUCTIONS = """
You are CareNest AI, a Vietnamese family health assistant.
Return only JSON matching the supplied schema.
Do not diagnose. Do not prescribe. Do not claim certainty.
Escalate breathing difficulty, unconsciousness, seizures, severe allergic reaction, or emergency wording as emergency.
Always include a safety disclaimer. Medication advice must tell the user to verify with a doctor or pharmacist.
""".strip()


def _build_payload(message: str) -> StructuredChatPayload:
    lowered = message.lower()

    if any(keyword in lowered for keyword in ["cap cuu", "kho tho", "ngat", "emergency"]):
        return StructuredChatPayload(
            intent="symptom_triage",
            summary="Trieu chung co dau hieu can xu ly khan.",
            advice=[
                "Khong tri hoan viec lien he co so y te.",
                "Neu nguoi benh dang kho tho hoac mat y thuc, hay goi cap cuu ngay.",
            ],
            risk_level="emergency",
            follow_up_questions=[],
            recommended_actions=[
                AiAction(type="go_emergency", label="Den cap cuu"),
                AiAction(type="book_doctor", label="Lien he bac si"),
            ],
            safety=StructuredChatSafety(
                needs_doctor=True,
                needs_emergency=True,
                disclaimer="AI chi ho tro dinh huong, khong thay the danh gia y khoa truc tiep.",
            ),
        )

    if any(keyword in lowered for keyword in ["thuoc", "don thuoc", "medication"]):
        return StructuredChatPayload(
            intent="medication_guidance",
            summary="Yeu cau lien quan den thuoc can duoc xac nhan lai truoc khi luu vao he thong.",
            advice=[
                "Kiem tra ten thuoc, lieu dung va tan suat truoc khi ap dung.",
                "Neu thong tin OCR hoac AI khong ro, hay sua tay truoc khi luu.",
            ],
            risk_level="medium",
            follow_up_questions=[
                "Thuoc nay dung cho ai?",
                "Ban muon tao lich dung thuoc hay chi can tom tat huong dan?",
            ],
            recommended_actions=[
                AiAction(type="track_symptom", label="Theo doi trieu chung"),
                AiAction(type="book_doctor", label="Hoi bac si"),
            ],
            safety=StructuredChatSafety(
                needs_doctor=True,
                needs_emergency=False,
                disclaimer="Khong tu dong xem AI la y lenh dieu tri.",
            ),
        )

    if any(keyword in lowered for keyword in ["tiem", "vaccine", "vaccination"]):
        return StructuredChatPayload(
            intent="general_health",
            summary="Noi dung lien quan den tiem chung can doi chieu lai lich va ho so suc khoe.",
            advice=[
                "Kiem tra ngay tiem, loai vaccine va moi luu y sau tiem.",
                "Neu tre sot cao hoac co phan ung manh, can lien he co so y te.",
            ],
            risk_level="medium",
            follow_up_questions=[
                "Ban muon tao lich tiem hay ghi nhan mui da tiem?",
            ],
            recommended_actions=[
                AiAction(type="track_symptom", label="Theo doi sau tiem"),
                AiAction(type="book_doctor", label="Lien he bac si"),
            ],
            safety=StructuredChatSafety(
                needs_doctor=True,
                needs_emergency=False,
                disclaimer="Thong tin tiem chung can duoc doi chieu voi huong dan cua bac si.",
            ),
        )

    return StructuredChatPayload(
        intent="unsupported" if not settings.ai_enabled or not settings.has_api_key else "general_health",
        summary="AI contract da san sang, nhung provider that se duoc bat o batch sau.",
        advice=[
            "Su dung du lieu co cau truc JSON lam nguon su that.",
            "Khong luu du lieu AI vao nghiep vu khi chua co buoc xac nhan.",
        ],
        risk_level="low",
        follow_up_questions=[
            "Ban can tom tat suc khoe hay huong dan tiep theo?",
        ],
        recommended_actions=[
            AiAction(type="ask_family", label="Hoi nguoi than"),
        ],
        safety=StructuredChatSafety(
            needs_doctor=False,
            needs_emergency=False,
            disclaimer="AI hien dang o che do foundation, chua goi model that.",
        ),
    )


@router.post("/chat", response_model=ChatReply)
async def chat_with_ai(request: ChatRequest):
    if settings.ai_enabled and settings.has_api_key:
        try:
            provider_payload = call_structured_response(
                model=settings.ai_model_chat,
                instructions=CHAT_INSTRUCTIONS,
                user_input=request.message,
                schema_name="carenest_chat_v1",
                schema=CHAT_JSON_SCHEMA,
            )
            payload = StructuredChatPayload(**provider_payload)
        except AiProviderError as error:
            raise HTTPException(status_code=502, detail=str(error)) from error
    else:
        payload = _build_payload(request.message)

    return ChatReply(
        reply=payload.summary,
        id=random.randint(1000, 9999),
        message_id=random.randint(1000, 9999),
        conversation_id=request.conversationId or random.randint(1, 100),
        structured=payload,
    )


@router.get("/conversations")
async def list_conversations():
    return {"conversations": [], "total": 0}
