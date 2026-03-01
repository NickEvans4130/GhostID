package com.ghostid.app.data.network

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MailTmService {

    @GET("/domains")
    suspend fun getDomains(): DomainsResponse

    @POST("/accounts")
    suspend fun createAccount(@Body request: CreateAccountRequest): AccountResponse

    @POST("/token")
    suspend fun getToken(@Body request: TokenRequest): TokenResponse

    @GET("/messages")
    suspend fun getMessages(
        @Header("Authorization") bearer: String,
        @Query("page") page: Int = 1,
    ): MessagesResponse

    @GET("/messages/{id}")
    suspend fun getMessage(
        @Header("Authorization") bearer: String,
        @Path("id") id: String,
    ): MessageDetail

    @DELETE("/messages/{id}")
    suspend fun deleteMessage(
        @Header("Authorization") bearer: String,
        @Path("id") id: String,
    )

    @DELETE("/accounts/{id}")
    suspend fun deleteAccount(
        @Header("Authorization") bearer: String,
        @Path("id") id: String,
    )
}
