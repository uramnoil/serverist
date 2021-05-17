package com.uramnoil.serverist.infrastracture.server

import com.uramnoil.serverist.infrastracture.user.Users
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.`java-time`.datetime
import java.time.LocalDateTime
import java.time.ZoneOffset

object Servers : UUIDTable("servers") {
    val name = varchar("name", 16)
    val owner = uuid("owner").references(Users.id)
    val createdAt = datetime("created_at")
    val address = varchar("address", 253).nullable()
    val port = integer("port").nullable()
    val description = varchar("description", 255)
}

fun LocalDateTime.toKotlinInstance() = toInstant(ZoneOffset.UTC).toKotlinInstant()

fun Instant.toJavaLocalDataTime() = LocalDateTime.ofInstant(toJavaInstant(), ZoneOffset.UTC)