package com.uramnoil.serverist

import com.apurebase.kgraphql.GraphQL
import com.benasher44.uuid.Uuid
import com.uramnoil.serverist.auth.application.unauthenticated.commands.AccountAlreadyExistsException
import com.uramnoil.serverist.auth.application.unauthenticated.commands.VerificationCodeHasAlreadyBeenSentException
import com.uramnoil.serverist.domain.common.exception.NotFoundException
import com.uramnoil.serverist.graphql.PageRequest
import com.uramnoil.serverist.graphql.serverSchema
import com.uramnoil.serverist.graphql.userSchema
import com.uramnoil.serverist.presenter.AuthController
import com.uramnoil.serverist.presenter.ServerController
import com.uramnoil.serverist.presenter.UserController
import com.uramnoil.serverist.serverist.server.application.queries.OrderBy
import com.uramnoil.serverist.serverist.server.infrastructure.Servers
import com.uramnoil.serverist.serverist.user.infrastructure.Users
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.features.ContentTransformationException
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.sessions.*
import io.ktor.util.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.inject
import org.slf4j.event.Level
import java.io.File
import java.util.*

data class AuthSession(val id: Uuid) : Principal

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

/**
 * 本番環境用
 */
@Suppress("unused")
fun Application.productMain() {
    install(StatusPages) {
        exception<ContentTransformationException> {
            call.respond(HttpStatusCode.BadRequest)
        }
        exception<Exception> {
            call.respond(HttpStatusCode.InternalServerError)
        }
    }

    install(ContentNegotiation) {
        json()
    }

    install(Sessions) {
        cookie<AuthSession>("SESSION", directorySessionStorage(File(".sessions"), cached = true)) {
            cookie.path = "/"
            cookie.maxAgeInSeconds = 1000
        }
    }

    install(CallLogging) {
        level = Level.DEBUG
        format {
            val userId = it.sessions.get<AuthSession>()?.id ?: "Guest"
            val ip = it.callId
            val status = it.response.status()
            val httpMethod = it.request.httpMethod.value
            val userAgent = it.request.headers["User-Agent"]
            val uri = it.request.uri
            "User id: $userId, User agent: $userAgent, Status: $status, HTTP method: $httpMethod, URI: $uri"
        }
    }

    createConnection()
    productKoin()
    routingAuth()
    buildGraphql()
}

fun Application.productKoin() {
    install(Koin) {
        modules()
    }
}

/**
 * コネクションプールの作成
 */
fun Application.createConnection() {
    environment.config.apply {
        val host = property("database.host").getString()
        val database = property("database.database").getString()
        val port = property("database.port").getString()

        Database.connect(
            url = "jdbc:mysql://${host}:${port}/${database}?characterEncoding=utf8&useSSL=false",
            driver = com.mysql.cj.jdbc.Driver::class.qualifiedName!!,
            user = property("database.user").getString(),
            password = property("database.password").getString()
        )

        transaction {
            SchemaUtils.create(Users, Servers)
        }
    }
}

/**
 * 認証関連のルーティング
 */
fun Application.routingAuth() = routing {
    val controller: AuthController by inject()
    post("login") {
        data class CredentialParameters(val email: String, val password: String)

        val (email, password) = call.receive<CredentialParameters>()

        // すでにログイン済みだった場合
        if (call.sessions.get<AuthSession>() != null) {
            call.respond(HttpStatusCode.OK)
            return@post
        }

        // ログイン処理
        val loginResult = controller.login(email, password)

        val id = loginResult.getOrElse {
            // サーバーエラー
            call.respond(HttpStatusCode.InternalServerError)
            return@post
        }

        id ?: run {
            // 不正なクレデンシャル
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        call.sessions.set(AuthSession(id))
    }

    // 登録
    post("signup") {
        data class EmailAndPassword(val email: String, val password: String)

        val (email, password) = call.receive<EmailAndPassword>()

        // サインアップ処理
        val createUserResult = controller.signUp(email, password)

        // 成功時
        createUserResult.onSuccess {
            call.respond(HttpStatusCode.OK)
        }

        // 失敗時
        createUserResult.onFailure {
            when (it) {
                // コードをすでに送信済み
                is VerificationCodeHasAlreadyBeenSentException -> {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        "The email owner must have already been received a code to verify."
                    )
                }
                // Emailがすでに使われている
                is AccountAlreadyExistsException -> {
                    call.respond(HttpStatusCode.BadRequest, "This email has already been used.")
                }
                // サーバーエラー
                else -> {
                    call.respond(HttpStatusCode.InternalServerError)
                    log.error(it)
                }
            }
        }
    }

    // Email認証
    get("activate") {
        data class AuthenticateUserId(val id: UUID)

        val (id) = call.receive<AuthenticateUserId>()

        val result = controller.activate(id)

        result.fold(
            {
                HttpStatusCode.OK
            },
            {
                when (it) {
                    is NotFoundException -> {
                        log.debug(it.message)
                        HttpStatusCode.BadRequest
                    }
                    else -> {
                        log.error(it)
                        HttpStatusCode.InternalServerError
                    }
                }
            }
        )
    }

    post("activate/resend") {
        call.respond(HttpStatusCode.InternalServerError)
    }
}

/**
 * GraphQL用のビルダ
 */
fun Application.buildGraphql() = install(GraphQL) {
    playground = true

    wrap {
        authenticate(optional = true, build = it)
    }

    context { call ->
        // AuthSession所有時にコンテキストへ追加
        call.authentication.principal<AuthSession>()?.let {
            +it
        }
    }

    schema {
        stringScalar<Uuid> {
            deserialize = { Uuid.fromString(it) }
            serialize = Uuid::toString
        }

        type<PageRequest>()
        enum<Sort>()
        enum<OrderBy>()

        val serverController: ServerController by inject()

        serverSchema(serverController)

        val userController: UserController by inject()

        userSchema(userController)
    }
}
