package com.connor.episode.features.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.connor.episode.domain.model.business.Owner
import com.connor.episode.domain.model.business.SelectType
import com.connor.episode.domain.model.uimodel.BleAction
import com.connor.episode.domain.model.uimodel.BleState
import com.connor.episode.domain.model.uimodel.TopBarAction
import com.connor.episode.domain.repository.PreferencesRepository
import com.connor.episode.domain.usecase.BleClientConnectUseCase
import com.connor.episode.domain.usecase.BleClientScanUseCase
import com.connor.episode.domain.usecase.BleServerStartAdvertisingUseCase
import com.connor.episode.domain.usecase.BleServerStartAndReadUseCase
import com.connor.episode.domain.usecase.CleanLogUseCase
import com.connor.episode.domain.usecase.GetPagingMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BleVIewModel @Inject constructor(
    private val bleClientConnectUseCase: BleClientConnectUseCase,
    private val bleClientScanUseCase: BleClientScanUseCase,
    private val bleServerStartAdvertisingUseCase: BleServerStartAdvertisingUseCase,
    private val bleServerStartAndReadUseCase: BleServerStartAndReadUseCase,
    private val preferencesRepository: PreferencesRepository,
    private val cleanLogUseCase: CleanLogUseCase,
    private val pagingMessageUseCase: GetPagingMessageUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(BleState())
    val state = _state.asStateFlow()

    val messagePagingFlow = pagingMessageUseCase(Owner.BLE).cachedIn(viewModelScope)

    private val devices = mutableListOf<BluetoothDevice>()

    @SuppressLint("MissingPermission")
    fun onAction(action: BleAction) = viewModelScope.launch {
        when (action) {
            BleAction.StartAdvertising -> TODO()
            BleAction.StartScanning -> TODO()
            BleAction.StartServer -> TODO()
            BleAction.TestConnect -> TODO()
            is BleAction.Top -> top(action.top)
            is BleAction.UpdateSelectType -> preferencesRepository.updateBlePref {
                it.copy(lastSelectType = SelectType.entries[action.i])
            }
        }
    }

    private suspend fun top(action: TopBarAction) = when (action) {
        TopBarAction.CleanLog -> {
            cleanLogUseCase(Owner.BLE)
        }

        TopBarAction.Close -> TODO()

        is TopBarAction.IsShowSettingDialog -> _state.update {
            it.copy(isShowSettingDialog = action.show)
        }
    }
}