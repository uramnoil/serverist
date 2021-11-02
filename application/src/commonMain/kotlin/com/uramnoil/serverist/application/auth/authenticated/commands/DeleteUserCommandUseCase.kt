package com.uramnoil.serverist.application.auth.authenticated.commands

import com.benasher44.uuid.Uuid

interface DeleteUserCommandUseCaseInputPort {
    fun execute(id: Uuid)
}

fun interface DeleteUserCommandUseCaseOutputPort {
    fun handle(result: Result<Unit>)
}