package com.uramnoil.serverist.infrastracture.user

import com.uramnoil.serverist.domain.user.models.*
import com.uramnoil.serverist.domain.user.repositories.UserRepository
import com.uramnoil.serverist.domain.user.services.CreateUserService
import java.util.*

class CreateUserServiceImpl(private val repository: UserRepository) : CreateUserService {
    override suspend fun new(
        accountId: AccountId,
        email: Email,
        hashedPassword: com.uramnoil.serverist.domain.kernel.models.HashedPassword,
        name: Name,
        description: Description
    ): User {
        val user = User(
            id = com.uramnoil.serverist.domain.kernel.models.UserId(UUID.randomUUID()),
            accountId = accountId,
            email = email,
            hashedPassword = hashedPassword,
            name = name,
            description = description
        )
        repository.insert(user)

        return user
    }
}