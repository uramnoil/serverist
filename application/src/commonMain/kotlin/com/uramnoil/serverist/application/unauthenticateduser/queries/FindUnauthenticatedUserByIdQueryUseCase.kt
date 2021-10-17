package com.uramnoil.serverist.application.unauthenticateduser.queries

import com.benasher44.uuid.Uuid
import com.uramnoil.serverist.application.unauthenticateduser.UnauthenticatedUser

interface FindUnauthenticatedUserByIdQueryUseCaseInputPort {
    fun execute(id: Uuid)
}

interface FindUnauthenticatedUserByIdQueryUseCaseOutputPort {
    fun handle(result: Result<UnauthenticatedUser?>)
}