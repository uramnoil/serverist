package com.uramnoil.serverist.infrastracture.user

import com.uramnoil.serverist.application.user.commands.DeleteUserCommand
import com.uramnoil.serverist.application.user.commands.DeleteUserCommandDto
import com.uramnoil.serverist.domain.models.user.Id
import com.uramnoil.serverist.domain.repositories.NotFoundException
import com.uramnoil.serverist.domain.repositories.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class ExposedDeleteUserCommand(private val repository: UserRepository, context: CoroutineContext) :
    DeleteUserCommand, CoroutineScope by CoroutineScope(context) {
    override fun execute(dto: DeleteUserCommandDto) {
        launch {
            repository.findByIdAsync(Id(dto.id)).await()?.let {
                repository.deleteAsync(it)
            } ?: throw NotFoundException("DeleteUserCommand#execute: ユーザー(ID: ${dto.id})が見つかりませんでした。")
        }
    }
}