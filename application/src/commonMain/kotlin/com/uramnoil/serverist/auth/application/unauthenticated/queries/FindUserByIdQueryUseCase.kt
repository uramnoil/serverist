package com.uramnoil.serverist.auth.application.unauthenticated.queries

import com.benasher44.uuid.Uuid

interface FindUserByIdQueryUseCaseInputPort {
    fun execute(id: Uuid)
}

fun interface FindUserByIdQueryUseCaseOutputPort {
    fun handle(result: Result<User?>)
}