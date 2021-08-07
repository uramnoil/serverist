package com.uramnoil.serverist.application.user.queries

import com.uramnoil.serverist.application.user.User

actual interface FindUserByNameQueryInputPort {
    fun execute(name: String, serversLimit: Long): User?
}

interface FindUserByNameQueryOutputPort {
    fun handle(result: Result<User?>)
}