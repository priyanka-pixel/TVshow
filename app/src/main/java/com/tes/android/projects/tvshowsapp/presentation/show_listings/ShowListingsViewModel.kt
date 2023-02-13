package com.tes.android.projects.tvshowsapp.presentation.show_listings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tes.android.projects.tvshowsapp.domain.model.ShowListing
import com.tes.android.projects.tvshowsapp.domain.repository.ShowRepository
import com.tes.android.projects.tvshowsapp.domain.use_case.FavoriteUseCase
import com.tes.android.projects.tvshowsapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShowListingsViewModel @Inject constructor(
    private val repository: ShowRepository,
    private val dispatcher: CoroutineDispatcher,
    private val favoriteUseCase: FavoriteUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(ShowListingsState())

    val uiState: StateFlow<ShowListingsState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onEvent(event: ShowListingsEvent) {
        when (event) {
            is ShowListingsEvent.Refresh -> {
                getShowListings(fetchFromRemote = true)
            }
            is ShowListingsEvent.OnSearchQueryChange -> {
                _uiState.value = ShowListingsState(searchQuery = event.query)
                searchJob = viewModelScope.launch {
                    delay(500L)
                    getShowListings()
                }
            }
            is ShowListingsEvent.OnFavoriteSelected -> {
                _uiState.value = ShowListingsState(show = event.show)
                addFavorite()

            }
            is ShowListingsEvent.DeleteFavorite-> {
                _uiState.value = _uiState.value.copy(id = event.id)
                deleteFavorite()
            }
            is ShowListingsEvent.LoadShows -> {
                getShowListings()
            }
        }
    }

    private fun deleteFavorite(
        id: Int = _uiState.value.id
    ) {
        viewModelScope.launch(dispatcher) {
            repository.deleteFavoriteById(id)
        }
    }

    private fun addFavorite(
        show: ShowListing = _uiState.value.show
    ) {
        viewModelScope.launch(dispatcher) {
            favoriteUseCase.addFavorite(show)
            //repository.insertFavoriteShowToDb(show)
            getShowListings()
        }
    }

    private fun getShowListings(
        query: String = _uiState.value.searchQuery.lowercase(),

        fetchFromRemote: Boolean = false
    ) {
        viewModelScope.launch(dispatcher) {
            repository.getShowListings(fetchFromRemote, query)
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            result.data?.let { listings ->
                                // copy of  current state so that we can change the listing
                                _uiState.value = _uiState.value.copy(shows = listings)
                            }
                            _uiState.value = _uiState.value.copy()
                        }
                        is Resource.Error -> {
                            _uiState.value = _uiState.value.copy(error = "Error message")
                        }
                        is Resource.Loading -> {
                            _uiState.value = _uiState.value.copy(isLoading = result.isLoading)
                        }
                    }
                }
        }
    }
}