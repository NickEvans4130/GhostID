package com.ghostid.app.domain.usecase

import com.ghostid.app.data.network.PhotoRepository
import com.ghostid.app.data.network.TempEmailRepository
import com.ghostid.app.data.repository.AliasRepository
import com.ghostid.app.domain.generator.AliasGenerator
import com.ghostid.app.domain.model.Alias
import javax.inject.Inject

class CreateAliasUseCase @Inject constructor(
    private val generator: AliasGenerator,
    private val repository: AliasRepository,
    private val photoRepository: PhotoRepository,
    private val tempEmailRepository: TempEmailRepository,
) {
    suspend operator fun invoke(): Alias {
        val alias = generator.generate()
        val birthYear = alias.dateOfBirth.take(4).toIntOrNull() ?: 1990
        val photoPath = photoRepository.fetchProfilePhoto(alias.gender, birthYear)
        val aliasWithPhoto = alias.copy(photoPath = photoPath)
        repository.saveAlias(aliasWithPhoto)
        runCatching { tempEmailRepository.createInboxForAlias(aliasWithPhoto) }
        return aliasWithPhoto
    }
}
