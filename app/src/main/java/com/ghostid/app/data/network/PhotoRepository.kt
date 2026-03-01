package com.ghostid.app.data.network

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "PhotoRepository"

@Singleton
class PhotoRepository @Inject constructor(
    private val randomUserService: RandomUserService,
    private val httpClient: OkHttpClient,
    @ApplicationContext private val context: Context,
) {

    /**
     * Returns a local file path (tiers 1–2) or a DiceBear URL (tier 3).
     * Never returns null — tier 3 always succeeds offline.
     *
     * [gender]    — "male", "female", or "neutral"
     * [birthYear] — four-digit year extracted from the alias DOB
     */
    suspend fun fetchProfilePhoto(gender: String, birthYear: Int): String {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val age = currentYear - birthYear
        val minAge = (age - 3).coerceAtLeast(18)
        val maxAge = (age + 3).coerceAtLeast(minAge)
        val apiGender = resolveGender(gender)

        fetchFromRandomUser(apiGender, minAge, maxAge)?.let {
            Log.d(TAG, "Source: randomuser.me")
            return it
        }

        fetchFromTPDNE()?.let {
            Log.d(TAG, "Source: thispersondoesnotexist.com")
            return it
        }

        Log.d(TAG, "Source: DiceBear fallback")
        return generateDiceBearUrl(gender, birthYear)
    }

    fun deleteCachedPhoto(path: String) {
        if (!path.startsWith("http")) {
            runCatching { File(path).delete() }
        }
    }

    // --- Tier 1: randomuser.me ---

    private suspend fun fetchFromRandomUser(gender: String, minAge: Int, maxAge: Int): String? {
        return try {
            val response = randomUserService.getUser(
                gender = gender,
                nationalities = "gb,us,au,ca,nz",
                include = "picture,dob",
            )
            val result = response.results.firstOrNull() ?: return null
            val returnedAge = result.dob.age

            if (returnedAge !in minAge..maxAge) {
                Log.d(TAG, "randomuser.me age mismatch: got $returnedAge, wanted $minAge-$maxAge")
                return null
            }

            downloadAndCache(result.picture.large)
        } catch (e: Exception) {
            Log.w(TAG, "randomuser.me failed: ${e.javaClass.simpleName}: ${e.message}")
            null
        }
    }

    // --- Tier 2: thispersondoesnotexist.com ---

    private suspend fun fetchFromTPDNE(): String? {
        return try {
            val seed = System.currentTimeMillis()
            downloadAndCache("https://thispersondoesnotexist.com?nocache=$seed")
        } catch (e: Exception) {
            Log.w(TAG, "TPDNE failed: ${e.javaClass.simpleName}: ${e.message}")
            null
        }
    }

    // --- Tier 3: DiceBear (always works, returns URL not file path) ---

    private fun generateDiceBearUrl(gender: String, birthYear: Int): String {
        val seed = "$gender$birthYear${System.currentTimeMillis()}"
        return "https://api.dicebear.com/7.x/personas/svg?seed=$seed"
    }

    // --- Shared download + cache ---

    private suspend fun downloadAndCache(url: String): String? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(url).build()
            val response = httpClient.newCall(request).execute()
            Log.d(TAG, "downloadAndCache $url → HTTP ${response.code}")
            if (!response.isSuccessful) return@withContext null
            val bytes = response.body?.bytes() ?: return@withContext null
            val dir = File(context.filesDir, "photos").also { it.mkdirs() }
            val file = File(dir, "${UUID.randomUUID()}.jpg")
            file.writeBytes(bytes)
            Log.d(TAG, "Cached ${bytes.size} bytes → ${file.name}")
            file.absolutePath
        } catch (e: Exception) {
            Log.w(TAG, "downloadAndCache failed for $url: ${e.javaClass.simpleName}: ${e.message}")
            null
        }
    }

    private fun resolveGender(gender: String): String = when (gender.lowercase()) {
        "female" -> "female"
        "male" -> "male"
        else -> if ((System.currentTimeMillis() % 2L) == 0L) "male" else "female"
    }
}
