package com.uramnoil.serverist.auth.application.unauthenticated.commands

import java.util.*


interface DeleteUserCommandUseCaseInputPort {
    suspend fun execute(id: UUID): Result<Unit>
}