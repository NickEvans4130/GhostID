package com.ghostid.app.presentation.navigation

sealed class Screen(val route: String) {
    data object Main : Screen("main")
    data object AliasDetail : Screen("alias/{aliasId}") {
        fun createRoute(aliasId: String) = "alias/$aliasId"
    }
    data object MessageDetail : Screen("inbox/{aliasId}/message/{messageId}") {
        fun createRoute(aliasId: String, messageId: String) = "inbox/$aliasId/message/$messageId"
    }
    data object PasswordVault : Screen("vault")
    data object Settings : Screen("settings")
}
