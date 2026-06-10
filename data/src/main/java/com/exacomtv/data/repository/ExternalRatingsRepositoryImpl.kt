package com.exacomtv.data.repository

import com.exacomtv.domain.model.ExternalRatings
import com.exacomtv.domain.model.ExternalRatingsLookup
import com.exacomtv.domain.model.Result
import com.exacomtv.domain.repository.ExternalRatingsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExternalRatingsRepositoryImpl @Inject constructor() : ExternalRatingsRepository {

    override suspend fun getRatings(lookup: ExternalRatingsLookup): Result<ExternalRatings> {
        return Result.success(ExternalRatings.unavailable())
    }
}