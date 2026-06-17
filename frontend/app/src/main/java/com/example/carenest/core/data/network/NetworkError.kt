package com.example.carenest.core.data.network

import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Response

fun <T> Response<ApiResponse<T>>.requireData(
    fallback: String,
    missingDataMessage: String = "Thiếu dữ liệu phản hồi",
): T {
    if (!isSuccessful) {
        throw IllegalStateException(errorMessage(fallback))
    }
    return body().requireData(fallback, missingDataMessage)
}

fun <T> Response<ApiResponse<List<T>>>.requireList(fallback: String): List<T> {
    if (!isSuccessful) {
        throw IllegalStateException(errorMessage(fallback))
    }
    return body().requireList(fallback)
}

fun <T> Response<ApiResponse<T>>.requireSuccess(fallback: String) {
    if (!isSuccessful) {
        throw IllegalStateException(errorMessage(fallback))
    }
    body().requireSuccess(fallback)
}

fun <T> ApiResponse<T>?.requireData(
    fallback: String,
    missingDataMessage: String = "Thiếu dữ liệu phản hồi",
): T {
    val envelope = this ?: throw IllegalStateException(fallback)
    if (!envelope.success) {
        throw IllegalStateException(envelope.message?.takeIf { it.isNotBlank() } ?: fallback)
    }
    return envelope.data
        ?: throw IllegalStateException(envelope.message?.takeIf { it.isNotBlank() } ?: missingDataMessage)
}

fun <T> ApiResponse<List<T>>?.requireList(fallback: String): List<T> {
    val envelope = this ?: throw IllegalStateException(fallback)
    if (!envelope.success) {
        throw IllegalStateException(envelope.message?.takeIf { it.isNotBlank() } ?: fallback)
    }
    return envelope.data.orEmpty()
}

fun <T> ApiResponse<T>?.requireSuccess(fallback: String) {
    val envelope = this ?: throw IllegalStateException(fallback)
    if (!envelope.success) {
        throw IllegalStateException(envelope.message?.takeIf { it.isNotBlank() } ?: fallback)
    }
}

fun Response<*>.errorMessage(fallback: String): String {
    val envelopeMessage = runCatching {
        (body() as? ApiResponse<*>)?.message
    }.getOrNull()
    if (!envelopeMessage.isNullOrBlank()) return envelopeMessage

    val rawError = runCatching { errorBody()?.string() }.getOrNull()
    val parsedMessage = parseErrorMessage(rawError)
    if (!parsedMessage.isNullOrBlank()) return parsedMessage

    val codeSuffix = code().takeIf { it > 0 }?.let { " ($it)" }.orEmpty()
    return fallback + codeSuffix
}

fun Throwable.userMessage(fallback: String): String {
    if (this is HttpException) {
        val rawError = runCatching { response()?.errorBody()?.string() }.getOrNull()
        val parsedMessage = parseErrorMessage(rawError)
        if (!parsedMessage.isNullOrBlank()) return parsedMessage

        val codeSuffix = code().takeIf { it > 0 }?.let { " ($it)" }.orEmpty()
        return fallback + codeSuffix
    }

    return localizedMessage?.takeIf { it.isNotBlank() }
        ?: message?.takeIf { it.isNotBlank() }
        ?: fallback
}

fun Throwable.userException(fallback: String): Exception {
    return if (this is Exception) {
        Exception(userMessage(fallback), this)
    } else {
        Exception(userMessage(fallback))
    }
}

private fun parseErrorMessage(rawError: String?): String? {
    if (rawError.isNullOrBlank()) return null

    return runCatching {
        val json = JSONObject(rawError)
        sequenceOf(
            json.optString("message"),
            json.optString("error"),
            json.optString("detail"),
        ).firstOrNull { it.isNotBlank() }
    }.getOrNull()
}
