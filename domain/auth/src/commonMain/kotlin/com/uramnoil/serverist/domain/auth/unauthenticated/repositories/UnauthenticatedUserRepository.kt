package com.uramnoil.serverist.domain.auth.unauthenticated.repositories

import com.uramnoil.serverist.domain.auth.unauthenticated.models.User
import com.uramnoil.serverist.domain.auth.unauthenticated.models.UserId

interface UnauthenticatedUserRepository {
    suspend fun insert(user: User): Result<Unit>
    suspend fun update(user: User): Result<Unit>
    suspend fun delete(user: User): Result<Unit>
    suspend fun findById(userId: UserId): Result<User?>
}