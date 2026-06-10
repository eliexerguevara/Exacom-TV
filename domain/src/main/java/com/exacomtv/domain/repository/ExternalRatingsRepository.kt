package com.exacomtv.domain.repository

import com.exacomtv.domain.model.ExternalRatings
import com.exacomtv.domain.model.ExternalRatingsLookup
import com.exacomtv.domain.model.Result

interface ExternalRatingsRepository {
    suspend fun getRatings(lookup: ExternalRatingsLookup): Result<ExternalRatings>
}