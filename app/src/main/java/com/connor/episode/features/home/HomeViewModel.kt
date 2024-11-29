package com.connor.episode.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connor.episode.domain.model.uimodel.HomeAction
import com.connor.episode.domain.model.uimodel.HomeState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(HomeState())
    val uiState = _uiState.asStateFlow()

    fun onAction(action: HomeAction) {
        viewModelScope.launch(Dispatchers.Main) {
            testFlow.collect {
                // 在主线程处理
            }
        }
        when (action) {
            is HomeAction.RouteChange -> _uiState.update { it.copy(currentRoute = action.route) }
        }
    }

    val testFlow = flow {
        emit(1)
        delay(1000)
        emit(2)
        delay(1000)
        emit(3)
    }.flowOn(Dispatchers.IO)

}