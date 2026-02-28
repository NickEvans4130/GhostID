package com.ghostid.app.data.network

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FaceImageRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val faceImageService: FaceImageService,
) {
    private val facesDir: File
        get() = File(context.filesDir, "faces").also { it.mkdirs() }

    /**
     * Downloads a synthetic face JPEG and caches it locally.
     * Returns the absolute path on success, null on failure.
     */
    suspend fun fetchAndCacheFace(): String? = withContext(Dispatchers.IO) {
        runCatching {
            val response = faceImageService.fetchFaceImage()
            if (!response.isSuccessful) return@runCatching null
            val body = response.body() ?: return@runCatching null
            val file = File(facesDir, "${UUID.randomUUID()}.jpg")
            file.outputStream().use { out ->
                body.byteStream().use { it.copyTo(out) }
            }
            file.absolutePath
        }.getOrNull()
    }

    fun deleteCachedFace(path: String) {
        runCatching { File(path).delete() }
    }

    /**
     * Dicebear fallback URL for use when the device is offline.
     * Returns an SVG avatar URL keyed on the alias name.
     */
    fun avatarFallbackUrl(seed: String): String =
        "https://api.dicebear.com/7.x/personas/svg?seed=${seed.replace(" ", "+")}"
}
