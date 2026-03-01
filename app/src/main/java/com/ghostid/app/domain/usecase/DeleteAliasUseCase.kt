package com.ghostid.app.domain.usecase

import com.ghostid.app.data.network.PhotoRepository
import com.ghostid.app.data.network.TempEmailRepository
import com.ghostid.app.data.repository.AliasRepository
import javax.inject.Inject

class DeleteAliasUseCase @Inject constructor(
    private val repository: AliasRepository,
    private val photoRepository: PhotoRepository,
    private val tempEmailRepository: TempEmailRepository,
) {
    suspend operator fun invoke(aliasId: String, photoPath: String?) {
        photoPath?.let { photoRepository.deleteCachedPhoto(it) }
        runCatching { tempEmailRepository.deleteInbox(aliasId) }
        repository.deleteAlias(aliasId)
    }
}
