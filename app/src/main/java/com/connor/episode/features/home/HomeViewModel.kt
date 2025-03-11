package com.connor.episode.features.home

import androidx.lifecycle.ViewModel
import com.connor.episode.domain.model.uimodel.HomeAction
import com.connor.episode.domain.model.uimodel.HomeState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(HomeState())
    val uiState = _uiState.asStateFlow()

    fun onAction(action: HomeAction) {
        when (action) {
            is HomeAction.RouteChange -> _uiState.update { it.copy(currentRoute = action.route) }
        }
    }
}