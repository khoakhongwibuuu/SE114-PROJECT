from fastapi import APIRouter
from schemas.chat_schemas import TTSRequest, TTSResponse, TTSResponseData

router = APIRouter()

# Một chuỗi base64 MP3 trống (hợp lệ hoặc gần hợp lệ) để tránh crash frontend
MOCK_MP3_BASE64 = "SUQzBAAAAAAAI1RTU0UAAAAPAAADTGF2ZjU4LjI5LjEwMAAAAAAAAAAAAAAA//tQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAWgAAAAAABzQkAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"

@router.post("/tts", response_model=TTSResponse)
async def generate_tts(request: TTSRequest):
    # Trả về âm thanh giả lập
    return TTSResponse(
        data=TTSResponseData(audio_base64=MOCK_MP3_BASE64)
    )
