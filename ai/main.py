from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
import uvicorn

from config import settings
from routers import chat, ocr, voice

app = FastAPI(title="CareNest AI Service")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(chat.router, prefix="/ai", tags=["Chat"])
app.include_router(ocr.router, prefix="/ai/ocr", tags=["OCR"])
app.include_router(voice.router, prefix="/ai/voice", tags=["Voice"])


@app.get("/")
def read_root():
    return {
        "message": "AI Service is running",
        "provider": settings.ai_provider,
        "ai_enabled": settings.ai_enabled,
        "ocr_enabled": settings.ocr_enabled,
        "has_api_key": settings.has_api_key,
    }


if __name__ == "__main__":
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
