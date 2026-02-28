package com.ghostid.app.domain.model

enum class PlatformCategory { COMMUNICATION, SOCIAL, PROFESSIONAL, EMAIL }

enum class Platform(val displayName: String, val category: PlatformCategory) {
    // Communication
    SIGNAL("Signal", PlatformCategory.COMMUNICATION),
    TELEGRAM("Telegram", PlatformCategory.COMMUNICATION),
    DISCORD("Discord", PlatformCategory.COMMUNICATION),
    WHATSAPP("WhatsApp", PlatformCategory.COMMUNICATION),
    MATRIX("Matrix/Element", PlatformCategory.COMMUNICATION),

    // Social
    INSTAGRAM("Instagram", PlatformCategory.SOCIAL),
    TWITTER("Twitter/X", PlatformCategory.SOCIAL),
    BLUESKY("Bluesky", PlatformCategory.SOCIAL),
    MASTODON("Mastodon", PlatformCategory.SOCIAL),
    TIKTOK("TikTok", PlatformCategory.SOCIAL),
    REDDIT("Reddit", PlatformCategory.SOCIAL),
    TUMBLR("Tumblr", PlatformCategory.SOCIAL),
    PINTEREST("Pinterest", PlatformCategory.SOCIAL),

    // Professional
    LINKEDIN("LinkedIn", PlatformCategory.PROFESSIONAL),
    GITHUB("GitHub", PlatformCategory.PROFESSIONAL),

    // Email
    EMAIL_PROTON("Email (Proton)", PlatformCategory.EMAIL),
    EMAIL_TUTA("Email (Tuta)", PlatformCategory.EMAIL),
    EMAIL_DISROOT("Email (Disroot)", PlatformCategory.EMAIL),
}
