package com.uramnoil.serverist.application.user.queries

import com.uramnoil.serverist.application.user.User

actual interface GetUserIfCorrectLoginInfoQuery {
    fun handle(result: Result<User?>)
}