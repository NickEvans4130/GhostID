package com.ghostid.app.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import java.io.File

@Composable
fun AliasAvatarImage(
    photoPath: String?,
    name: String,
    initials: String,
    accentColor: Color,
    size: Dp = 64.dp,
    fallbackSeed: String = name,
) {
    val context = LocalContext.current
    val localFileExists = photoPath != null && File(photoPath).exists()

    if (localFileExists) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data(File(photoPath!!))
                .crossfade(true)
                .build(),
            contentDescription = "Profile photo of $name",
            modifier = Modifier.size(size).clip(CircleShape),
            contentScale = ContentScale.Crop,
            loading = { InitialsAvatar(initials, accentColor, size) },
            error = { InitialsAvatar(initials, accentColor, size) },
        )
    } else {
        val avatarUrl = "https://api.dicebear.com/7.x/personas/svg?seed=${fallbackSeed.replace(" ", "+")}"
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data(avatarUrl)
                .decoderFactory(SvgDecoder.Factory())
                .crossfade(true)
                .build(),
            contentDescription = "Avatar for $name",
            modifier = Modifier.size(size).clip(CircleShape),
            contentScale = ContentScale.Crop,
            loading = { InitialsAvatar(initials, accentColor, size) },
            error = { InitialsAvatar(initials, accentColor, size) },
        )
    }
}

@Composable
fun InitialsAvatar(
    initials: String,
    accentColor: Color,
    size: Dp = 64.dp,
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(accentColor.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials.take(2).uppercase(),
            fontSize = (size.value * 0.35f).sp,
            fontWeight = FontWeight.Bold,
            color = accentColor,
        )
    }
}
