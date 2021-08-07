package com.uramnoil.serverist.application.unauthenticateduser.queries

import com.benasher44.uuid.Uuid
import com.uramnoil.serverist.application.unauthenticateduser.UnauthenticatedUser

actual interface FindUnauthenticatedUserByIdQueryInputPort {
    suspend fun execute(id: Uuid): Result<UnauthenticatedUser?>
}