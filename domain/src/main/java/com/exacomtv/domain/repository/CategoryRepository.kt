package com.exacomtv.domain.repository

import com.exacomtv.domain.model.Category
import com.exacomtv.domain.model.ContentType
import com.exacomtv.domain.model.Result
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getCategories(providerId: Long): Flow<List<Category>>
    suspend fun setCategoryProtection(
        providerId: Long,
        categoryId: Long,
        type: ContentType,
        isProtected: Boolean
    ): Result<Unit>
}
