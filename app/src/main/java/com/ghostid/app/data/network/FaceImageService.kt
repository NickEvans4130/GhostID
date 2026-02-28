package com.ghostid.app.data.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET

interface FaceImageService {
    /**
     * Fetches a random synthetic face JPEG from thispersondoesnotexist.com.
     * Each call returns a different image.
     */
    @GET("/")
    suspend fun fetchFaceImage(): Response<ResponseBody>
}
