package com.ghostid.app.data.network

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.security.SecureRandom
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private data class FakeFaceResponse(
    val age: Int,
    val gender: String,
    @SerializedName("image_url") val imageUrl: String,
)

@Singleton
class FaceImageRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient,
    private val gson: Gson,
) {
    private val rng = SecureRandom()

    private val facesDir: File
        get() = File(context.filesDir, "faces").also { it.mkdirs() }

    /**
     * Fetches an age- and gender-matched face from fakeface.rest, caches it locally.
     *
     * Fallback chain:
     *   1. fakeface.rest with tight age range (±2 years)
     *   2. fakeface.rest with loose age range (±10 years)
     *   3. null — caller renders a Dicebear avatar instead
     *
     * [gender] — "male", "female", or "neutral" (neutral randomly selects male/female)
     * [age]    — calculated from the alias DOB
     */
    suspend fun fetchAndCacheFace(gender: String, age: Int): String? =
        withContext(Dispatchers.IO) {
            val apiGender = resolveApiGender(gender)

            val imageUrl =
                tryFetch(apiGender, (age - 2).coerceAtLeast(18), age + 2)
                    ?: tryFetch(apiGender, (age - 10).coerceAtLeast(18), age + 10)
                    ?: return@withContext null

            downloadAndCache(imageUrl)
        }

    private fun resolveApiGender(gender: String): String = when (gender.lowercase()) {
        "female" -> "female"
        "male" -> "male"
        else -> if (rng.nextBoolean()) "male" else "female"
    }

    private fun tryFetch(gender: String, minAge: Int, maxAge: Int): String? = runCatching {
        val url = "https://fakeface.rest/face/json" +
            "?gender=$gender&minimum_age=$minAge&maximum_age=$maxAge"
        val request = Request.Builder().url(url).build()
        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@runCatching null
            val body = response.body?.string() ?: return@runCatching null
            gson.fromJson(body, FakeFaceResponse::class.java)?.imageUrl
        }
    }.getOrNull()

    private fun downloadAndCache(imageUrl: String): String? = runCatching {
        val request = Request.Builder().url(imageUrl).build()
        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@runCatching null
            val body = response.body ?: return@runCatching null
            val file = File(facesDir, "${UUID.randomUUID()}.jpg")
            file.outputStream().use { out -> body.byteStream().copyTo(out) }
            file.absolutePath
        }
    }.getOrNull()

    fun deleteCachedFace(path: String) {
        runCatching { File(path).delete() }
    }

    fun avatarFallbackUrl(seed: String): String =
        "https://api.dicebear.com/7.x/personas/svg?seed=${seed.replace(" ", "+")}"
}
