package com.uramnoil.serverist.domain.service.repositories

import com.uramnoil.serverist.domain.login.models.user.User
import kotlinx.coroutines.Deferred

interface UserRepository {
    fun storeAsync(user: User): Deferred<Unit>
    fun deleteAsync(user: User): Deferred<Unit>
}