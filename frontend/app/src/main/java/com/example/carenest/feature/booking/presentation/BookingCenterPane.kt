package com.example.carenest.feature.booking.presentation

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.carenest.core.presentation.theme.AppRadius
import com.example.carenest.core.presentation.theme.AppSpacing
import com.example.carenest.core.presentation.theme.CardBackground
import com.example.carenest.core.presentation.theme.CareNestTextStyles
import com.example.carenest.core.presentation.theme.PageBackground
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.core.presentation.theme.TextPrimary
import com.example.carenest.core.presentation.theme.TextSecondary
import com.example.carenest.feature.booking.domain.model.BookingResponse
import com.example.carenest.feature.booking.domain.model.BookingStatus
import com.example.carenest.feature.booking.domain.model.BookingRequestType
import com.example.carenest.feature.ekyc.domain.model.DoctorSummary
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun BookingCenterPane(
    viewModel: BookingCenterViewModel,
    currentProfileId: Long?,
    canAccessDoctorWorkspace: Boolean,
    onNavigateToAppointments: (Long) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var confirmTarget by remember { mutableStateOf<BookingResponse?>(null) }
    var rejectTarget by remember { mutableStateOf<BookingResponse?>(null) }
    var cancelTarget by remember { mutableStateOf<BookingResponse?>(null) }

    LaunchedEffect(canAccessDoctorWorkspace) {
        viewModel.refresh(canAccessDoctorWorkspace)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CardBackground)
    ) {
        if (state.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)
        ) {
            item {
                StatusBanner(
                    error = state.error,
                    message = state.message,
                    loading = state.isSubmitting
                )
            }

            if (canAccessDoctorWorkspace) {
                item {
                    DoctorWorkspaceHeader()
                }
                if (state.doctorBookings.isEmpty() && !state.isLoading) {
                    item {
                        EmptyState(
                            title = "Chưa có yêu cầu chờ xử lý",
                            subtitle = "Khi bệnh nhân gửi yêu cầu, bác sĩ sẽ xác nhận giờ cụ thể ngay tại đây."
                        )
                    }
                } else {
                    items(state.doctorBookings, key = { it.id }) { booking ->
                        DoctorBookingCard(
                            booking = booking,
                            onConfirm = { confirmTarget = booking },
                            onReject = { rejectTarget = booking },
                            onCancel = { cancelTarget = booking }
                        )
                    }
                }
            } else {
                item {
                    PatientBookingComposer(
                        doctors = state.doctors,
                        selectedDoctorId = state.selectedDoctorId,
                        currentProfileId = currentProfileId,
                        requestType = state.requestType,
                        preferredSchedule = state.preferredSchedule,
                        patientNote = state.patientNote,
                        isSubmitting = state.isSubmitting,
                        onDoctorSelected = viewModel::selectDoctor,
                        onTypeSelected = viewModel::updateRequestType,
                        onPreferredScheduleChanged = viewModel::updatePreferredSchedule,
                        onPatientNoteChanged = viewModel::updatePatientNote,
                        onSubmit = { viewModel.createBooking(currentProfileId, canAccessDoctorWorkspace) }
                    )
                }

                item {
                    SectionHeader(
                        title = "Trung tâm đặt lịch",
                        subtitle = "Lịch đã xác nhận sẽ tự động đồng bộ sang Lịch tái khám."
                    )
                }

                if (state.patientBookings.isEmpty() && !state.isLoading) {
                    item {
                        EmptyState(
                            title = "Chưa có yêu cầu đặt lịch",
                            subtitle = "Hãy chọn bác sĩ và gửi nhu cầu để bắt đầu quy trình xác nhận lịch."
                        )
                    }
                } else {
                    items(state.patientBookings, key = { it.id }) { booking ->
                        PatientBookingCard(
                            booking = booking,
                            onOpenAppointments = onNavigateToAppointments,
                            onCancel = { cancelTarget = booking }
                        )
                    }
                }
            }
        }
    }

    confirmTarget?.let { booking ->
        ConfirmScheduleDialog(
            booking = booking,
            onDismiss = { confirmTarget = null },
            onConfirm = { scheduledAtIso, confirmedLocation, confirmedNote ->
                viewModel.confirmSchedule(
                    bookingId = booking.id,
                    scheduledAtIso = scheduledAtIso,
                    confirmedLocation = confirmedLocation,
                    confirmedNote = confirmedNote,
                    canAccessDoctorWorkspace = canAccessDoctorWorkspace
                )
                confirmTarget = null
            }
        )
    }

    rejectTarget?.let { booking ->
        ReasonDialog(
            title = "Từ chối yêu cầu",
            placeholder = "Lý do từ chối",
            confirmLabel = "Từ chối",
            onDismiss = { rejectTarget = null },
            onConfirm = { reason ->
                viewModel.rejectBooking(booking.id, reason, canAccessDoctorWorkspace)
                rejectTarget = null
            }
        )
    }

    cancelTarget?.let { booking ->
        ReasonDialog(
            title = "Hủy lịch / yêu cầu",
            placeholder = "Lý do hủy (không bắt buộc)",
            confirmLabel = "Hủy yêu cầu",
            requireReason = false,
            onDismiss = { cancelTarget = null },
            onConfirm = { reason ->
                viewModel.cancelBooking(booking.id, reason, canAccessDoctorWorkspace)
                cancelTarget = null
            }
        )
    }
}

@Composable
private fun DoctorWorkspaceHeader() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(AppRadius.lg)
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Text(
                text = "Điều phối lịch bác sĩ",
                style = CareNestTextStyles.titleMd,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "Mỗi lần xác nhận giờ cụ thể sẽ tạo hoặc cập nhật đúng một lịch trong Lịch tái khám của bệnh nhân.",
                style = CareNestTextStyles.bodyMd,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun PatientBookingComposer(
    doctors: List<DoctorSummary>,
    selectedDoctorId: Long?,
    currentProfileId: Long?,
    requestType: BookingRequestType,
    preferredSchedule: String,
    patientNote: String,
    isSubmitting: Boolean,
    onDoctorSelected: (Long) -> Unit,
    onTypeSelected: (BookingRequestType) -> Unit,
    onPreferredScheduleChanged: (String) -> Unit,
    onPatientNoteChanged: (String) -> Unit,
    onSubmit: () -> Unit
) {
    val canSubmit = !isSubmitting &&
        currentProfileId != null &&
        selectedDoctorId != null &&
        patientNote.isNotBlank()

    Card(
        colors = CardDefaults.cardColors(containerColor = PageBackground),
        shape = RoundedCornerShape(AppRadius.lg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            SectionHeader(
                title = "Yêu cầu đặt lịch với bác sĩ",
                subtitle = if (currentProfileId != null) {
                    "Hồ sơ đang áp dụng: #$currentProfileId. Nếu cần đổi người khám, hãy đổi hồ sơ đang hoạt động trước khi gửi."
                } else {
                    "Chưa có hồ sơ sức khỏe đang hoạt động. Cần chọn đúng hồ sơ trước khi gửi yêu cầu."
                }
            )

            Text(
                text = "Chọn bác sĩ",
                style = CareNestTextStyles.labelMd,
                color = TextPrimary
            )
            if (doctors.isEmpty()) {
                EmptyState(
                    title = "Chưa có bác sĩ khả dụng",
                    subtitle = "Danh sách sẽ xuất hiện khi có bác sĩ đã xác thực trong hệ thống."
                )
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    items(doctors, key = { it.id }) { doctor ->
                        DoctorChipCard(
                            doctor = doctor,
                            selected = doctor.id == selectedDoctorId,
                            onClick = { onDoctorSelected(doctor.id) }
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                FilterChip(
                    selected = requestType == BookingRequestType.OFFLINE_CLINIC,
                    onClick = { onTypeSelected(BookingRequestType.OFFLINE_CLINIC) },
                    label = { Text("Khám trực tiếp") }
                )
                FilterChip(
                    selected = requestType == BookingRequestType.ONLINE_CHAT,
                    onClick = { onTypeSelected(BookingRequestType.ONLINE_CHAT) },
                    label = { Text("Tư vấn online") }
                )
            }

            OutlinedTextField(
                value = preferredSchedule,
                onValueChange = onPreferredScheduleChanged,
                label = { Text("Khung giờ mong muốn") },
                placeholder = { Text("Ví dụ: chiều thứ 6 hoặc sau 19:00") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            OutlinedTextField(
                value = patientNote,
                onValueChange = onPatientNoteChanged,
                label = { Text("Nhu cầu / triệu chứng *") },
                placeholder = { Text("Mô tả ngắn để bác sĩ sắp lịch phù hợp") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            if (patientNote.isBlank()) {
                Text(
                    text = "Vui lòng mô tả ngắn nhu cầu hoặc triệu chứng trước khi gửi.",
                    style = CareNestTextStyles.bodySm,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = onSubmit,
                enabled = canSubmit,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.width(18.dp).height(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Gửi yêu cầu đặt lịch")
                }
            }
        }
    }
}

@Composable
private fun DoctorChipCard(
    doctor: DoctorSummary,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.width(220.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(AppRadius.lg)
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = doctor.fullName,
                style = CareNestTextStyles.labelMd,
                color = TextPrimary
            )
            Text(
                text = doctor.specialty ?: "Chưa cập nhật chuyên khoa",
                style = CareNestTextStyles.bodySm,
                color = TextSecondary
            )
            Text(
                text = doctor.hospitalName ?: "Chưa cập nhật cơ sở",
                style = CareNestTextStyles.bodySm,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun PatientBookingCard(
    booking: BookingResponse,
    onOpenAppointments: (Long) -> Unit,
    onCancel: () -> Unit
) {
    BookingCardShell(booking = booking) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            if (booking.status == BookingStatus.APPROVED && booking.appointmentId != null) {
                val targetProfileId = booking.healthProfileId?.takeIf { it > 0L }
                Button(
                    onClick = { targetProfileId?.let(onOpenAppointments) },
                    modifier = Modifier.weight(1f),
                    enabled = targetProfileId != null
                ) {
                    Text("Mở Lịch tái khám")
                }
            }
            if (booking.status == BookingStatus.PENDING || booking.status == BookingStatus.APPROVED) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Hủy yêu cầu")
                }
            }
        }
    }
}

@Composable
private fun DoctorBookingCard(
    booking: BookingResponse,
    onConfirm: () -> Unit,
    onReject: () -> Unit,
    onCancel: () -> Unit
) {
    BookingCardShell(booking = booking) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            if (booking.status == BookingStatus.PENDING || booking.status == BookingStatus.APPROVED) {
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (booking.status == BookingStatus.APPROVED) "Cập nhật lịch" else "Xác nhận lịch")
                }
            }
            if (booking.status == BookingStatus.PENDING) {
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Từ chối")
                }
            }
            if (booking.status == BookingStatus.APPROVED) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Hủy lịch")
                }
            }
        }
    }
}

@Composable
private fun BookingCardShell(
    booking: BookingResponse,
    footer: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = PageBackground),
        shape = RoundedCornerShape(AppRadius.lg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = booking.patientFullName,
                        style = CareNestTextStyles.titleMd,
                        color = TextPrimary
                    )
                    Text(
                        text = booking.doctorFullName
                            ?.takeIf { it.isNotBlank() }
                            ?.let { "BS. $it" }
                            ?: "BS. Chưa cập nhật",
                        style = CareNestTextStyles.bodyMd,
                        color = TextSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .background(Color(0xFFEFF6FF), RoundedCornerShape(AppRadius.sm))
                        .padding(horizontal = AppSpacing.sm, vertical = 6.dp)
                ) {
                    Text(
                        text = statusLabel(booking.status),
                        style = CareNestTextStyles.labelSm,
                        color = PrimaryBlue
                    )
                }
            }

            booking.healthProfileName?.takeIf { it.isNotBlank() }?.let { MetaRow("Hồ sơ", it) }
            MetaRow("Loại", if (booking.requestType == BookingRequestType.ONLINE_CHAT) "Tư vấn online" else "Khám trực tiếp")
            booking.doctorSpecialty?.takeIf { it.isNotBlank() }?.let { MetaRow("Chuyên khoa", it) }
            booking.doctorHospitalName?.takeIf { it.isNotBlank() }?.let { MetaRow("Cơ sở", it) }
            booking.preferredTimeNote?.takeIf { it.isNotBlank() }?.let { MetaRow("Mong muốn", it) }
            booking.note.takeIf { it.isNotBlank() }?.let { MetaRow("Nhu cầu", it) }
            booking.scheduledAt?.let { MetaRow("Lịch đã xác nhận", formatSchedule(it)) }
            booking.confirmedLocation?.takeIf { it.isNotBlank() }?.let { MetaRow("Địa điểm", it) }
            booking.confirmedNote?.takeIf { it.isNotBlank() }?.let { MetaRow("Ghi chú bác sĩ", it) }
            booking.rejectReason?.takeIf { it.isNotBlank() }?.let { MetaRow("Lý do từ chối", it) }
            booking.cancellationReason?.takeIf { it.isNotBlank() }?.let { MetaRow("Lý do hủy", it) }
            if (booking.appointmentId != null && booking.status == BookingStatus.APPROVED) {
                MetaRow("Đồng bộ lịch", "Đã ghi vào Lịch tái khám (#${booking.appointmentId})")
            }

            footer()
        }
    }
}

@Composable
private fun MetaRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = CareNestTextStyles.labelSm,
            color = TextSecondary
        )
        Text(
            text = value,
            style = CareNestTextStyles.bodyMd,
            color = TextPrimary
        )
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = CareNestTextStyles.titleMd,
            color = TextPrimary
        )
        Text(
            text = subtitle,
            style = CareNestTextStyles.bodyMd,
            color = TextSecondary
        )
    }
}

@Composable
private fun EmptyState(title: String, subtitle: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(AppRadius.lg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Text(
                text = title,
                style = CareNestTextStyles.titleMd,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                style = CareNestTextStyles.bodyMd,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun StatusBanner(
    error: String?,
    message: String?,
    loading: Boolean
) {
    when {
        loading -> {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                shape = RoundedCornerShape(AppRadius.lg)
            ) {
                Text(
                    text = "Đang xử lý thay đổi lịch...",
                    modifier = Modifier.padding(AppSpacing.md),
                    style = CareNestTextStyles.bodyMd,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        error != null -> {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = RoundedCornerShape(AppRadius.lg)
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(AppSpacing.md),
                    style = CareNestTextStyles.bodyMd,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        message != null -> {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(AppRadius.lg)
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(AppSpacing.md),
                    style = CareNestTextStyles.bodyMd,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun ConfirmScheduleDialog(
    booking: BookingResponse,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    val context = LocalContext.current
    val zoneId = remember { ZoneId.systemDefault() }
    val initialDateTime = remember(booking.scheduledAt) {
        booking.scheduledAt?.let { Instant.parse(it).atZone(zoneId).toLocalDateTime() } ?: LocalDateTime.now().plusDays(1)
    }
    var selectedDate by remember(booking.id) { mutableStateOf(initialDateTime.toLocalDate()) }
    var selectedTime by remember(booking.id) { mutableStateOf(initialDateTime.toLocalTime().withSecond(0).withNano(0)) }
    var confirmedLocation by remember(booking.id) { mutableStateOf(booking.confirmedLocation.orEmpty()) }
    var confirmedNote by remember(booking.id) { mutableStateOf(booking.confirmedNote.orEmpty()) }

    val datePickerDialog = remember(selectedDate) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
            },
            selectedDate.year,
            selectedDate.monthValue - 1,
            selectedDate.dayOfMonth
        )
    }

    val timePickerDialog = remember(selectedTime) {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                selectedTime = LocalTime.of(hourOfDay, minute)
            },
            selectedTime.hour,
            selectedTime.minute,
            true
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val scheduledAt = LocalDateTime.of(selectedDate, selectedTime)
                        .atZone(zoneId)
                        .toInstant()
                        .toString()
                    onConfirm(scheduledAt, confirmedLocation, confirmedNote)
                }
            ) {
                Text("Lưu lịch")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        },
        title = {
            Text(if (booking.status == BookingStatus.APPROVED) "Cập nhật lịch đã xác nhận" else "Xác nhận lịch cụ thể")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                OutlinedTextField(
                    value = selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    onValueChange = {},
                    label = { Text("Ngày hẹn") },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePickerDialog.show() }
                )
                OutlinedTextField(
                    value = selectedTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                    onValueChange = {},
                    label = { Text("Giờ hẹn") },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { timePickerDialog.show() }
                )
                OutlinedTextField(
                    value = confirmedLocation,
                    onValueChange = { confirmedLocation = it },
                    label = { Text("Địa điểm / phòng khám") },
                    placeholder = { Text(if (booking.requestType == BookingRequestType.ONLINE_CHAT) "Link / nền tảng online (nếu có)" else "Ví dụ: Phòng khám Nhi, tầng 2") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = confirmedNote,
                    onValueChange = { confirmedNote = it },
                    label = { Text("Hướng dẫn thêm") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        }
    )
}

@Composable
private fun ReasonDialog(
    title: String,
    placeholder: String,
    confirmLabel: String,
    requireReason: Boolean = true,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { onConfirm(reason) },
                enabled = !requireReason || reason.isNotBlank()
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        },
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text(placeholder) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
        }
    )
}

private fun statusLabel(status: BookingStatus): String = when (status) {
    BookingStatus.PENDING -> "Chờ xử lý"
    BookingStatus.APPROVED -> "Đã xác nhận lịch"
    BookingStatus.REJECTED -> "Đã từ chối"
    BookingStatus.CANCELLED -> "Đã hủy"
    BookingStatus.ACTIVE -> "Đang tư vấn"
    BookingStatus.COMPLETED -> "Hoàn tất"
    BookingStatus.RESTRICTED -> "Đã khóa"
}

private fun formatSchedule(iso: String): String {
    return try {
        val instant = Instant.parse(iso)
        val formatter = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy", Locale("vi", "VN"))
        instant.atZone(ZoneId.systemDefault()).format(formatter)
    } catch (_: Exception) {
        iso
    }
}
