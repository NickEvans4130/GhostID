package com.ghostid.app.presentation.ui.components

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
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

    when {
        // Local JPEG from randomuser.me or TPDNE download
        photoPath != null && !photoPath.startsWith("http") && File(photoPath).exists() -> {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(File(photoPath))
                    .crossfade(true)
                    .build(),
                contentDescription = "Profile photo of $name",
                modifier = Modifier.size(size).clip(CircleShape),
                contentScale = ContentScale.Crop,
                loading = { InitialsAvatar(initials, accentColor, size) },
                error = { InitialsAvatar(initials, accentColor, size) },
            )
        }

        // DiceBear SVG URL returned from tier 3 fallback
        photoPath != null && photoPath.startsWith("http") -> {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(photoPath)
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

        // No photo at all — show initials
        else -> InitialsAvatar(initials, accentColor, size)
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
