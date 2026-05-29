from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from routers import chat, voice
import uvicorn

app = FastAPI(title="CareNest AI Service")

# Cấu hình CORS để frontend React Native có thể gọi API
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Đăng ký các router
app.include_router(chat.router, prefix="/ai", tags=["Chat"])
app.include_router(voice.router, prefix="/ai/voice", tags=["Voice"])

@app.get("/")
def read_root():
    return {"message": "AI Service is running!"}

if __name__ == "__main__":
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
