package com.ghostid.app.domain.usecase

import com.ghostid.app.data.network.FaceImageRepository
import com.ghostid.app.data.network.TempEmailRepository
import com.ghostid.app.data.repository.AliasRepository
import com.ghostid.app.domain.generator.AliasGenerator
import com.ghostid.app.domain.model.Alias
import java.time.LocalDate
import java.time.Period
import javax.inject.Inject

class CreateAliasUseCase @Inject constructor(
    private val generator: AliasGenerator,
    private val repository: AliasRepository,
    private val faceImageRepository: FaceImageRepository,
    private val tempEmailRepository: TempEmailRepository,
) {
    suspend operator fun invoke(): Alias {
        val alias = generator.generate()
        val age = Period.between(LocalDate.parse(alias.dateOfBirth), LocalDate.now()).years
        val photoPath = faceImageRepository.fetchAndCacheFace(alias.gender, age)
        val aliasWithPhoto = alias.copy(photoPath = photoPath)
        repository.saveAlias(aliasWithPhoto)
        // Inbox creation is best-effort — failure does not block alias creation
        runCatching { tempEmailRepository.createInboxForAlias(aliasWithPhoto) }
        return aliasWithPhoto
    }
}
