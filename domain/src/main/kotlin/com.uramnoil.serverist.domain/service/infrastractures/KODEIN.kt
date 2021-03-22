package com.uramnoil.serverist.domain.service.infrastractures

import com.uramnoil.serverist.service.repositories.ServerRepository
import com.uramnoil.serverist.service.repositories.UserRepository
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton
import kotlin.coroutines.CoroutineContext

fun buildDomain(context: CoroutineContext) = DI {
    bind<ServerRepository>() with singleton {
        TODO()
    }

    bind<UserRepository>() with singleton {
        TODO()
    }
}