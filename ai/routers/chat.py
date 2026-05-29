from fastapi import APIRouter
from schemas.chat_schemas import ChatRequest, ChatReply
import time
import random

router = APIRouter()

# Dữ liệu nội bộ mô phỏng
mock_medicine_data = [
    "Hôm nay bạn cần uống Panadol Extra 500mg (1 viên, Sáng) và Amoxicillin 250mg (1 viên, Trưa). Đừng quên uống thuốc sau khi ăn nhé!",
    "Bạn có lịch hẹn tiêm chủng Mũi 2 - Viêm gan B vào ngày mai lúc 9:00 sáng tại Phòng khám CareNest.",
    "Tình hình sức khỏe của bé Nguyễn Văn A đang phát triển rất tốt, chiều cao và cân nặng đạt chuẩn.",
    "Tủ thuốc gia đình bạn hiện có Panadol, Amoxicillin và Vitamin C. Chưa có thuốc nào sắp hết hạn.",
    "Tôi là trợ lý AI CareNest. Tôi có thể giúp bạn quản lý lịch uống thuốc, theo dõi sức khỏe và trả lời các thắc mắc về y tế."
]

@router.post("/chat", response_model=ChatReply)
async def chat_with_ai(request: ChatRequest):
    message_lower = request.message.lower()
    reply = ""

    # Rule-based logic cơ bản để trả lời theo context
    if "thuốc" in message_lower and "hôm nay" in message_lower:
        reply = mock_medicine_data[0]
    elif "hết hạn" in message_lower or "tủ thuốc" in message_lower:
        reply = mock_medicine_data[3]
    elif "sức khỏe" in message_lower or "tóm tắt" in message_lower:
        reply = mock_medicine_data[2]
    elif "lịch hẹn" in message_lower or "tiêm" in message_lower:
        reply = mock_medicine_data[1]
    else:
        # Generic response hoặc random
        reply = "Cảm ơn bạn đã chia sẻ. " + mock_medicine_data[4]

    # Giả lập thời gian suy nghĩ
    time.sleep(1)

    return ChatReply(
        reply=reply,
        id=random.randint(1000, 9999),
        message_id=random.randint(1000, 9999),
        conversation_id=request.conversationId or random.randint(1, 100),
    )

@router.get("/conversations")
async def list_conversations():
    return {"conversations": [], "total": 0}
