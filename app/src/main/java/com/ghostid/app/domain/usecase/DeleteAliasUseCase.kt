package com.ghostid.app.domain.usecase

import com.ghostid.app.data.network.FaceImageRepository
import com.ghostid.app.data.network.TempEmailRepository
import com.ghostid.app.data.repository.AliasRepository
import javax.inject.Inject

class DeleteAliasUseCase @Inject constructor(
    private val repository: AliasRepository,
    private val faceImageRepository: FaceImageRepository,
    private val tempEmailRepository: TempEmailRepository,
) {
    suspend operator fun invoke(aliasId: String, photoPath: String?) {
        photoPath?.let { faceImageRepository.deleteCachedFace(it) }
        runCatching { tempEmailRepository.deleteInbox(aliasId) }
        repository.deleteAlias(aliasId)
    }
}
