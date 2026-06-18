package com.example.carenest

object AppConfig {
    // Set HOST_IP in local.properties when testing on a physical device.
    // "10.0.2.2" works for the Android Emulator.
    const val HOST_IP = BuildConfig.HOST_IP
    val AI_CHAT_ENABLED = BuildConfig.AI_CHAT_ENABLED

    const val BACKEND_URL = "http://$HOST_IP:8080"
    const val WEBSOCKET_URL = "ws://$HOST_IP:8080/api/v1/ws"
}
