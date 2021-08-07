package com.uramnoil.serverist.application.server.commands

import com.benasher44.uuid.Uuid


actual interface UpdateServerCommandInputPort {
    suspend fun execute(
        id: Uuid,
        name: String,
        address: String?,
        port: Int?,
        description: String
    ): Result<Unit>
}