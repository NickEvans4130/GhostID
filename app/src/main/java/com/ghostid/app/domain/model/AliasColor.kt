package com.ghostid.app.domain.model

import androidx.compose.ui.graphics.Color

val aliasAccentColors = listOf(
    0xFF6C63FF.toInt(), // purple
    0xFFE94560.toInt(), // red/pink
    0xFF00C9A7.toInt(), // teal
    0xFFFF6B6B.toInt(), // coral
    0xFF4ECDC4.toInt(), // cyan
    0xFF45B7D1.toInt(), // sky blue
    0xFFF7DC6F.toInt(), // yellow
    0xFFBB8FCE.toInt(), // lavender
    0xFF82E0AA.toInt(), // mint
    0xFFF0B27A.toInt(), // peach
    0xFF85C1E9.toInt(), // light blue
    0xFFF1948A.toInt(), // salmon
)

fun Int.toComposeColor(): Color = Color(this.toLong() or 0xFF000000L)
