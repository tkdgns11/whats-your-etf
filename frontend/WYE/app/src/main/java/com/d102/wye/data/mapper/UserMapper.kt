package com.d102.wye.data.mapper

import com.d102.wye.data.remote.dto.response.SocialAccountResponse
import com.d102.wye.data.remote.dto.response.UserProfileResponse
import com.d102.wye.domain.model.SocialAccount
import com.d102.wye.domain.model.UserProfile

fun UserProfileResponse.toDomain() = UserProfile(
    id = id,
    email = email,
    nickname = nickname,
    profileImage = profileImage,
    role = role,
    isActive = isActive,
    lastLoginAt = lastLoginAt,
    createdAt = createdAt,
    socialAccounts = socialAccounts.map { it.toDomain() }
)

fun SocialAccountResponse.toDomain() = SocialAccount(
    provider = provider,
    email = email,
    isPrimary = isPrimary,
    linkedAt = linkedAt
)
