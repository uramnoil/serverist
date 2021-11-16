package com.uramnoil.serverist.auth.application.unauthenticated.queries


/**
 *
 */
interface FindUserByEmailQueryUseCaseInputPort {
    /**
     *
     */
    suspend fun execute(email: String)
}

interface FindUserByEmailQueryUseCaseOutputPort {
    /**
     *
     */
    suspend fun handle(result: Result<User?>)
}