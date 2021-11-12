package com.uramnoil.serverist.backend.integrate

import com.icegreen.greenmail.util.GreenMail
import com.icegreen.greenmail.util.GreenMailUtil
import com.icegreen.greenmail.util.ServerSetupTest
import com.uramnoil.serverist.auth.infrastructure.HashPasswordServiceImpl
import com.uramnoil.serverist.domain.auth.kernel.model.HashedPassword
import com.uramnoil.serverist.domain.auth.kernel.model.Password
import com.uramnoil.serverist.mainModule
import com.uramnoil.serverist.serverist.infrastructure.Servers
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.ktor.application.*
import io.ktor.config.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import com.uramnoil.serverist.auth.infrastructure.authenticated.Users as AuthenticatedUsers
import com.uramnoil.serverist.auth.infrastructure.unauthenticated.Users as UnauthenticatedUsers
import com.uramnoil.serverist.serverist.infrastructure.Users as ServeristUsers

class AuthTest : FunSpec({
    val greenMail = GreenMail(ServerSetupTest.SMTP)

    fun Application.mailConfig() {
        (environment.config as MapApplicationConfig).apply {
            put("mail.host", "localhost")
            put("mail.port", "3025")
            put("mail.from", "test@serverist.com")
            put("mail.user", "")
            put("mail.password", "")
            put("mail.activate_url", "http://localhost:8080/activate")
        }
    }

    beforeSpec {
        Database.connect(
            "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver",
            user = "sa",
            password = ""
        )

        transaction {
            SchemaUtils.create(
                ServeristUsers,
                Servers,
                AuthenticatedUsers,
                UnauthenticatedUsers
            )
        }

        transaction {
            AuthenticatedUsers.insert {
                it[AuthenticatedUsers.id] = UUID.randomUUID()
                it[AuthenticatedUsers.email] = "uramnoil@example.com"
                it[AuthenticatedUsers.hashedPassword] = HashPasswordServiceImpl().hash(Password("abcd1234")).value
            }
        }
    }

    beforeTest {
        greenMail.start()
    }

    afterTest {
        greenMail.stop()
    }

    test("/signup test") {
        withTestApplication(
            moduleFunction = {
                mailConfig()
                mainModule()
            }
        ) {
            val email = "hoge@example.com"
            val password = "abcd1234"
            val hashPasswordService = HashPasswordServiceImpl()

            with(handleRequest(HttpMethod.Post, "/signup") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(Json.encodeToString(mapOf("email" to email, "password" to password)))
            }) {
                assertEquals(HttpStatusCode.OK, response.status(), response.content)
                assertTrue(greenMail.waitForIncomingEmail(3000, 1))
                val mail = greenMail.receivedMessages.firstOrNull()
                assertNotNull(mail)

                val row = transaction {
                    UnauthenticatedUsers.select { UnauthenticatedUsers.email eq email }.firstOrNull()
                }
                row shouldNotBe null

                // Email
                row!![UnauthenticatedUsers.email] shouldBe email
                // Password
                hashPasswordService.check(
                    Password(password),
                    HashedPassword(row[UnauthenticatedUsers.hashedPassword])
                ) shouldBe true
                // EmailBody
                "http://localhost:8080/activate?code=${row[UnauthenticatedUsers.activateCode]}" shouldBe GreenMailUtil.getBody(
                    mail
                )
            }
        }
    }

    test("/activate test") {
        withTestApplication(
            moduleFunction = {
                mailConfig()
                mainModule()
            }
        ) {
            val email = "hoge@example.com"
            val password = "abcd1234"
            handleRequest(HttpMethod.Post, "/signup") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(Json.encodeToString(mapOf("email" to email, "password" to password)))
            }

            val unauthenticatedRow = transaction {
                UnauthenticatedUsers.select { UnauthenticatedUsers.email eq email }.firstOrNull()
            }
            assertNotNull(unauthenticatedRow)
            val activationCode = unauthenticatedRow[UnauthenticatedUsers.activateCode]

            with(handleRequest(HttpMethod.Get, "/activate?code=$activationCode")) {
                response.status() shouldBe HttpStatusCode.OK
            }

            val authenticatedRow = transaction {
                AuthenticatedUsers.select { AuthenticatedUsers.email eq email }.firstOrNull()
            }
            assertNotNull(authenticatedRow)

            val id = authenticatedRow[AuthenticatedUsers.id]
            val serveristRow = transaction {
                ServeristUsers.select { ServeristUsers.id eq id }.firstOrNull()
            }
            assertNotNull(serveristRow)
        }
    }

    test("/login test") {
        withTestApplication(
            moduleFunction = {
                mailConfig()
                mainModule()
            }
        ) {
            val email = "hoge@example.com"
            val password = "abcd1234"
            val hashedPassword = HashPasswordServiceImpl().hash(Password(password)).value
            transaction {
                AuthenticatedUsers.insert {
                    it[AuthenticatedUsers.id] = UUID.randomUUID()
                    it[AuthenticatedUsers.email] = email
                    it[AuthenticatedUsers.hashedPassword] = hashedPassword
                }
            }
            cookiesSession {
                with(handleRequest(HttpMethod.Post, "/login") {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(Json.encodeToString(mapOf("email" to email, "password" to password)))
                }) {
                    response.cookies["AUTH"] shouldNotBe null
                }
            }
        }
    }

    test("/withdrawal test") {
        withTestApplication(
            moduleFunction = {
                mailConfig()
                mainModule()
            }
        ) {
            val email = "hoge@example.com"
            val password = "abcd1234"
            val hashedPassword = HashPasswordServiceImpl().hash(Password(password)).value
            val uuid = UUID.randomUUID()
            transaction {
                AuthenticatedUsers.insert {
                    it[AuthenticatedUsers.id] = uuid
                    it[AuthenticatedUsers.email] = email
                    it[AuthenticatedUsers.hashedPassword] = hashedPassword
                }
                ServeristUsers.insert {
                    it[ServeristUsers.id] = uuid
                    it[ServeristUsers.accountId] = "hoge"
                    it[ServeristUsers.name] = "hoge"
                    it[ServeristUsers.description] = "hoge"
                }
            }
            cookiesSession {
                with(handleRequest(HttpMethod.Post, "/login") {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(Json.encodeToString(mapOf("email" to email, "password" to password)))
                }) {
                    response.status() shouldBe HttpStatusCode.OK
                }
                with(handleRequest(HttpMethod.Post, "/withdrawal")) {
                    response.status() shouldBe HttpStatusCode.OK
                }

                val row = transaction {
                    AuthenticatedUsers.select { AuthenticatedUsers.email eq email }.firstOrNull()
                }

                assertNull(row)
            }
        }
    }

    test("/logout test") {
        withTestApplication(
            moduleFunction = {
                mailConfig()
                mainModule()
            }
        ) {
            val email = "hoge@example.com"
            val password = "abcd1234"
            val hashedPassword = HashPasswordServiceImpl().hash(Password(password)).value
            val uuid = UUID.randomUUID()
            transaction {
                AuthenticatedUsers.insert {
                    it[AuthenticatedUsers.id] = uuid
                    it[AuthenticatedUsers.email] = email
                    it[AuthenticatedUsers.hashedPassword] = hashedPassword
                }
                ServeristUsers.insert {
                    it[ServeristUsers.id] = uuid
                    it[ServeristUsers.accountId] = "hoge"
                    it[ServeristUsers.name] = "hoge"
                    it[ServeristUsers.description] = "hoge"
                }
            }
            cookiesSession {
                with(handleRequest(HttpMethod.Post, "/login") {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(Json.encodeToString(mapOf("email" to email, "password" to password)))
                }) {
                    response.status() shouldBe HttpStatusCode.OK
                }
                with(handleRequest(HttpMethod.Post, "/logout")) {
                    response.status() shouldBe HttpStatusCode.OK
                }
            }
        }
    }
})