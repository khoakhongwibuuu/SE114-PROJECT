package com.example.carenest

object AppConfig {
    // Thay đổi IP này thành IP IPv4 của máy tính bạn trong mạng LAN (Wi-Fi)
    // Dùng ipconfig (Windows) hoặc ifconfig (Mac/Linux) để tìm.
    // "10.0.2.2" chỉ hoạt động trên Android Emulator (máy ảo).
    const val HOST_IP = BuildConfig.HOST_IP

    const val BACKEND_URL = "http://$HOST_IP:8080"
    const val WEBSOCKET_URL = "ws://$HOST_IP:8080/api/v1/ws"
    const val AI_BACKEND_URL = "http://$HOST_IP:8000/"
}
