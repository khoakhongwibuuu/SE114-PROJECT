package com.example.carenest.feature.social.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.carenest.core.presentation.theme.CareNestTextStyles
import com.example.carenest.core.presentation.theme.OutlineVariant
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.core.presentation.theme.TextPrimary
import com.example.carenest.core.presentation.theme.TextSecondary
import com.example.carenest.feature.social.domain.model.AuthorRole
import com.example.carenest.feature.social.domain.model.Comment

@Composable
fun CommentItem(
    comment: Comment,
    onReplyClick: (Comment) -> Unit,
    modifier: Modifier = Modifier
) {
    val isReply = comment.parentCommentId != null
    val startPadding = if (isReply) 32.dp else 0.dp

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = startPadding, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        if (!comment.authorAvatar.isNullOrBlank()) {
            AsyncImage(
                model = comment.authorAvatar,
                contentDescription = "Avatar of ${comment.authorName}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(OutlineVariant)
            )
        } else {
            val initials = comment.authorName.take(1).uppercase()
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(PrimaryBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    style = CareNestTextStyles.labelMd,
                    color = PrimaryBlue
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = comment.authorName,
                            style = CareNestTextStyles.labelMd,
                            color = TextPrimary,
                            maxLines = 1
                        )
                        if (comment.authorRole == AuthorRole.DOCTOR) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Filled.Verified,
                                contentDescription = "Verified Doctor",
                                tint = PrimaryBlue,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = comment.content,
                        style = CareNestTextStyles.bodyMd,
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Text(
                    text = formatCreatedAt(comment.createdAt),
                    style = CareNestTextStyles.bodySm,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Phản hồi",
                    style = CareNestTextStyles.labelSm,
                    color = PrimaryBlue,
                    modifier = Modifier.clickable { onReplyClick(comment) }
                )
            }
        }
    }
}

private fun formatCreatedAt(createdAt: String?): String {
    if (createdAt.isNullOrBlank()) return ""
    return try {
        if (createdAt.contains("T")) {
            val datePart = createdAt.substringBefore("T")
            val timePart = createdAt.substringAfter("T").substringBefore(".")
            "${timePart.substring(0, 5)} $datePart"
        } else {
            createdAt
        }
    } catch (_: Exception) {
        createdAt
    }
}
