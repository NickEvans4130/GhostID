package com.ghostid.app.data.network

import retrofit2.http.GET
import retrofit2.http.Query

interface RandomUserService {

    @GET("/api/")
    suspend fun getUser(
        @Query("gender") gender: String,
        @Query("nat") nationalities: String,
        @Query("inc") include: String,
    ): RandomUserResponse
}

data class RandomUserResponse(val results: List<RandomUserResult>)
data class RandomUserResult(val picture: Picture, val dob: Dob)
data class Picture(val large: String, val medium: String, val thumbnail: String)
data class Dob(val date: String, val age: Int)
