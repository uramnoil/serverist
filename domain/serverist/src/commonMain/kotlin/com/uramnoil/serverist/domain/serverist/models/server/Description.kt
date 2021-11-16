package com.uramnoil.serverist.domain.serverist.models.server

data class Description(val value: String) {
    init {
        if (value.length > 255) {
            throw IllegalArgumentException("255文字以下にしてください。")
        }
    }
}