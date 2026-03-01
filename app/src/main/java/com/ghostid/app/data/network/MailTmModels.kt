package com.ghostid.app.data.network

import com.google.gson.annotations.SerializedName

data class CreateAccountRequest(val address: String, val password: String)
data class TokenRequest(val address: String, val password: String)
data class AccountResponse(val id: String, val address: String)
data class TokenResponse(val token: String, val id: String)

data class DomainsResponse(
    @SerializedName("hydra:member") val domains: List<DomainEntry>,
    @SerializedName("hydra:totalItems") val total: Int = 0,
)

data class DomainEntry(val domain: String)

data class MessagesResponse(
    @SerializedName("hydra:member") val messages: List<MessageSummary>,
    @SerializedName("hydra:totalItems") val total: Int = 0,
)

data class MessageSummary(
    val id: String,
    val from: AddressObject,
    val subject: String,
    val intro: String,
    val seen: Boolean,
    val createdAt: String,
)

data class MessageDetail(
    val id: String,
    val from: AddressObject,
    val subject: String,
    val html: List<String>,
    val text: String,
    val createdAt: String,
)

data class AddressObject(val address: String, val name: String)
