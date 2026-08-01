package com.openfinds.app.feature.devices

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.openfinds.app.core.domain.model.TrustedDevice

/** A device's avatar: the user's chosen photo if set, otherwise a colored initial. */
@Composable
fun DeviceAvatar(
    device: TrustedDevice,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
) {
    if (device.avatarImageUri != null) {
        AsyncImage(
            model = device.avatarImageUri,
            contentDescription = "${device.name}'s avatar",
            modifier = modifier.size(size).clip(CircleShape),
        )
    } else {
        Box(
            modifier = modifier.size(size).background(Color(device.avatarColorArgb), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(device.name.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}
