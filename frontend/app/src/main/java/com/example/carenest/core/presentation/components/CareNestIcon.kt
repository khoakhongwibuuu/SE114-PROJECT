package com.example.carenest.core.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

private fun iconVector(name: String): ImageVector = when (name) {
    "home" -> Icons.Filled.Home
    "group", "users" -> Icons.Filled.Group
    "globe" -> Icons.Filled.Public
    "medication", "pill" -> Icons.Filled.Medication
    "chat-processing", "smart_toy" -> Icons.AutoMirrored.Filled.Chat
    "person" -> Icons.Filled.Person
    "notifications" -> Icons.Filled.Notifications
    "notifications_active" -> Icons.Filled.NotificationsActive
    "search" -> Icons.Filled.Search
    "add" -> Icons.Filled.Add
    "add_circle" -> Icons.Filled.AddCircle
    "arrow_back" -> Icons.AutoMirrored.Filled.ArrowBack
    "arrow_forward" -> Icons.AutoMirrored.Filled.ArrowForward
    "chevron_right" -> Icons.AutoMirrored.Filled.KeyboardArrowRight
    "expand_more" -> Icons.Filled.ExpandMore
    "check" -> Icons.Filled.Check
    "check_circle" -> Icons.Filled.CheckCircle
    "favorite" -> Icons.Filled.Favorite
    "send" -> Icons.AutoMirrored.Filled.Send
    "close" -> Icons.Filled.Close
    "camera", "photo_camera" -> Icons.Filled.PhotoCamera
    "photo_library", "image" -> Icons.Filled.Collections
    "inventory_2", "home_storage" -> Icons.Filled.Inventory2
    "schedule", "access_time" -> Icons.Filled.Schedule
    "vaccines", "syringe" -> Icons.Filled.Vaccines
    "medical_services" -> Icons.Filled.MedicalServices
    "local_hospital" -> Icons.Filled.LocalHospital
    "lock", "lock_outline", "password" -> Icons.Filled.Lock
    "mail", "email" -> Icons.Filled.Email
    "visibility" -> Icons.Filled.Visibility
    "visibility_off" -> Icons.Filled.VisibilityOff
    "settings" -> Icons.Filled.Settings
    "more_vert" -> Icons.Filled.MoreVert
    "qr_code" -> Icons.Filled.QrCode
    "share" -> Icons.Filled.Share
    "person_add" -> Icons.Filled.PersonAdd
    "verified" -> Icons.Filled.Verified
    "call", "phone" -> Icons.Filled.Call
    else -> Icons.Filled.AccountCircle
}

@Composable
fun CareNestIcon(
    name: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
) {
    Icon(
        imageVector = iconVector(name),
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint,
    )
}
