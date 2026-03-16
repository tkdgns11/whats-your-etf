package com.d102.wye.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class UserProfileResponse(
    @SerializedName("id")
    val id: Long,
    @SerializedName("email")
    val email: String,
    @SerializedName("nickname")
    val nickname: String,
    @SerializedName("profileImage")
    val profileImage: String?,
    @SerializedName("role")
    val role: String,
    @SerializedName("isActive")
    val isActive: Boolean,
    @SerializedName("lastLoginAt")
    val lastLoginAt: String?,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("socialAccounts")
    val socialAccounts: List<SocialAccountResponse> = emptyList()
)

data class SocialAccountResponse(
    @SerializedName("provider")
    val provider: String,
    @SerializedName("email")
    val email: String?,
    @SerializedName("isPrimary")
    val isPrimary: Boolean,
    @SerializedName("linkedAt")
    val linkedAt: String
)
