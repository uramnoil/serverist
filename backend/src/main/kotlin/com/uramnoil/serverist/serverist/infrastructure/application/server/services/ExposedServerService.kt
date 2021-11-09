package com.uramnoil.serverist.serverist.infrastructure.application.server.services

import com.benasher44.uuid.Uuid
import com.uramnoil.serverist.serverist.application.server.services.ServerService
import com.uramnoil.serverist.serverist.infrastructure.Servers
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class ExposedServerService : ServerService {
    override suspend fun checkUserIsOwnerOfServer(ownerId: Uuid, serverId: Uuid): Result<Boolean> = kotlin.runCatching {
        newSuspendedTransaction {
            val result = Servers.select { (Servers.id eq serverId) and (Servers.owner eq ownerId) }
            result.firstOrNull() != null
        }
    }
}