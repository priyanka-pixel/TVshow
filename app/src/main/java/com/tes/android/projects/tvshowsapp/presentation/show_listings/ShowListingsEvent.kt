package com.tes.android.projects.tvshowsapp.presentation.show_listings

import com.tes.android.projects.tvshowsapp.domain.model.ShowListing


sealed class ShowListingsEvent {
    object Refresh : ShowListingsEvent()
    object LoadShows : ShowListingsEvent()
    data class OnSearchQueryChange(val query: String) : ShowListingsEvent()
    data class OnFavoriteSelected(val show:ShowListing):ShowListingsEvent()
    data class DeleteFavorite(val id: Int) : ShowListingsEvent()

}
