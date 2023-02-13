package com.tes.android.projects.tvshowsapp.domain.use_case

import com.tes.android.projects.tvshowsapp.domain.model.ShowListing
import com.tes.android.projects.tvshowsapp.domain.repository.ShowRepository
import javax.inject.Inject


class FavoriteUseCase @Inject constructor(
    private val repository: ShowRepository
) {
    suspend fun addFavorite(show:ShowListing) {
        repository.insertFavoriteShowToDb(show)
    }
}