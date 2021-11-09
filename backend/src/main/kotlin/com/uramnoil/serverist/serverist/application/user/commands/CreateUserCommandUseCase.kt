package com.uramnoil.serverist.serverist.application.user.commands

import java.util.*

interface CreateUserCommandUseCaseInputPort {
    suspend fun execute(
        id: UUID,
        accountId: String,
        name: String,
        description: String
    ): Result<UUID>
}