package com.connor.episode.features.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.connor.episode.core.utils.asciiToHexString
import com.connor.episode.core.utils.hexStringToAscii
import com.connor.episode.core.utils.logCat
import com.connor.episode.domain.model.business.ModelType
import com.connor.episode.domain.model.business.NetResult
import com.connor.episode.domain.model.business.Owner
import com.connor.episode.domain.model.business.SelectType
import com.connor.episode.domain.model.error.BleError
import com.connor.episode.domain.model.preference.BleDevice
import com.connor.episode.domain.model.uimodel.AdvertisingState
import com.connor.episode.domain.model.uimodel.BleAction
import com.connor.episode.domain.model.uimodel.BleState
import com.connor.episode.domain.model.uimodel.BottomBarAction
import com.connor.episode.domain.model.uimodel.ConnectState
import com.connor.episode.domain.model.uimodel.ScanState
import com.connor.episode.domain.model.uimodel.ServerState
import com.connor.episode.domain.model.uimodel.TopBarAction
import com.connor.episode.domain.usecase.BleClientConnectUseCase
import com.connor.episode.domain.usecase.BleClientScanUseCase
import com.connor.episode.domain.usecase.BleServerStartAdvertisingUseCase
import com.connor.episode.domain.usecase.BleServerStartAndReadUseCase
import com.connor.episode.domain.usecase.CleanLogUseCase
import com.connor.episode.domain.usecase.GetPagingMessageUseCase
import com.connor.episode.domain.usecase.ObservePrefUseCase
import com.connor.episode.domain.usecase.ResendUseCase
import com.connor.episode.domain.usecase.SendDataUseCase
import com.connor.episode.domain.usecase.UpdatePreferencesUseCase
import com.connor.episode.domain.usecase.WriteMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BleVIewModel @Inject constructor(
    private val bleClientConnectUseCase: BleClientConnectUseCase,
    private val bleClientScanUseCase: BleClientScanUseCase,
    private val bleServerStartAdvertisingUseCase: BleServerStartAdvertisingUseCase,
    private val bleServerStartAndReadUseCase: BleServerStartAndReadUseCase,
    private val updatePreferencesUseCase: UpdatePreferencesUseCase,
    private val observePrefUseCase: ObservePrefUseCase,
    private val cleanLogUseCase: CleanLogUseCase,
    private val pagingMessageUseCase: GetPagingMessageUseCase,
    private val resendUseCase: ResendUseCase,
    private val writeMessageUseCase: WriteMessageUseCase,
    private val sendDataUseCase: SendDataUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(BleState())
    val state = _state.asStateFlow()

    val messagePagingFlow = pagingMessageUseCase(Owner.BLE).cachedIn(viewModelScope)

    private var advertisingJob: Job? = null
    private var serverJob: Job? = null

    private var scanJob: Job? = null
    private var connectJob: Job? = null

    private var resendJob: Job? = null

    private val devices = HashMap<String, BluetoothDevice>()  //先简单使用名字

    init {
        viewModelScope.launch {
            observePrefUseCase.ble.onEach {
                _state.update { state ->
                    state.copy(
                        currentType = it.lastSelectType,
                        bottomBarSettings = it.settings,
                        connectDevice = it.device
                    )
                }
            }.launchIn(this)
        }
    }

    @SuppressLint("MissingPermission")
    fun onAction(action: BleAction) = viewModelScope.launch {
        when (action) {
            is BleAction.Top -> top(action.top)
            BleAction.ClickAdvertise -> handleAdvertise()

            BleAction.ClickServer -> handleServer()

            is BleAction.ClickConnect -> connectDevice(action.name)

            BleAction.ClickScan -> handleScan()

            BleAction.ClickLink -> handleLink()

            is BleAction.Bottom -> bottom(action.bottom)
        }
    }

    private fun CoroutineScope.handleLink() {
        when (state.value.connectState) {
            ConnectState.Connected -> {
                _state.update {
                    it.copy(connectState = ConnectState.Disconnected, info = "Disconnected")
                }
                connectJob?.cancel()
            }

            else -> connectDevice(state.value.connectDevice.name)
        }
    }

    private suspend fun bottom(action: BottomBarAction) {
        when (action) {
            is BottomBarAction.Expand -> _state.update {
                it.copy(expandedBottomBar = action.expand)
            }

            is BottomBarAction.OnMessageChange -> _state.update { state ->
                state.copy(message = action.msg)
            }

            is BottomBarAction.ReceiveFormatSelect -> updatePreferencesUseCase.ble {
                it.copy(settings = it.settings.copy(receiveFormat = action.idx))
            }

            is BottomBarAction.Resend -> {
                resendJob?.cancel()
                resendJob = viewModelScope.launch {
                    resendUseCase(action.resend, Owner.BLE).collect {
                        _state.value = state.value.copy(info = it)
                    }
                }
            }

            is BottomBarAction.ResendSeconds -> updatePreferencesUseCase.ble {
                it.copy(settings = it.settings.copy(resendSeconds = action.seconds))
            }

            is BottomBarAction.Send -> if (
                (state.value.connectState == ConnectState.Connected || state.value.serverState == ServerState.Active) &&
                state.value.message.text.isNotEmpty()
            ) _state.update {
                send(action)
            }

            is BottomBarAction.SendFormatSelect -> sendFormatSelect(action)
        }
    }

    private suspend fun sendFormatSelect(action: BottomBarAction.SendFormatSelect) {
        if (_state.value.bottomBarSettings.sendFormat == action.idx) return
        val text = _state.value.message.text
        val state = if (action.idx == 0) {
            val hex = text.asciiToHexString()
            _state.value.copy(
                message = TextFieldValue(
                    text = hex,
                    selection = TextRange(hex.length)
                )
            )
        } else {
            if (text.replace(" ", "").length % 2 != 0) return
            val ascii = text.replace(" ", "").hexStringToAscii()
            _state.value.copy(
                message = TextFieldValue(text = ascii, selection = TextRange(ascii.length))
            )

        }
        _state.update { state }
        updatePreferencesUseCase.ble {
            it.copy(settings = it.settings.copy(sendFormat = action.idx))
        }
    }

    private suspend fun send(action: BottomBarAction.Send) = run {
        val byte = writeMessageUseCase(action.msg, Owner.BLE)
        when (state.value.currentType) {
            SelectType.Server -> sendDataUseCase(action.msg, ModelType.BLEServer, Owner.BLE)
            SelectType.Client -> sendDataUseCase(action.msg, ModelType.BLEClient, Owner.BLE)
        }.fold(
            ifLeft = { state.value.copy(info = it) },
            ifRight = { state.value.copy(message = TextFieldValue()) }
        )
    }


    @SuppressLint("MissingPermission")
    private fun CoroutineScope.handleScan() {
        when (state.value.scanState) {
            ScanState.Scanning -> {
                scanJob?.cancel()
                _state.update {
                    it.copy(scanState = ScanState.Stopped, info = "Scan Stopped")
                }
            }

            else -> {
                _state.update {
                    it.copy(scanState = ScanState.Scanning, info = "Scanning")
                }
                scanJob?.cancel()
                scanJob = bleClientScanUseCase().onEach { result ->
                    result.fold(
                        ifLeft = { err ->
                            val state = if (err is BleError.ScanTimeout)
                                _state.value.copy(
                                    scanState = ScanState.Stopped,
                                    info = "Scan Timeout"
                                )
                            else _state.value.copy(
                                scanState = ScanState.Error,
                                info = "Scan Error ${err.msg}"
                            )
                            _state.update { state }
                            scanJob?.cancel()
                        },
                        ifRight = { device ->
                            device.name?.let { name ->
                                devices[name] = device
                                _state.update {
                                    it.copy(deviceNames = it.deviceNames + name)
                                }
                            }
                            "Scan: ${device.name} ${device.address}".logCat()
                        }
                    )
                }.launchIn(this)
            }
        }
    }

    private fun CoroutineScope.connectDevice(name: String) = launch {
        val device = devices[name] ?: run {
            _state.update {
                it.copy(
                    info = "Device not found",
                    connectState = ConnectState.Error,
                    connectDevice = BleDevice("Error", "")
                )
            }
            return@launch
        }
        connectJob?.cancel()
        connectJob = bleClientConnectUseCase(device).onEach { err ->
            _state.update {
                it.copy(
                    connectState = ConnectState.Error,
                    info = "Connect Error ${err.msg}"
                )
            }
        }.launchIn(this)
        _state.update {
            it.copy(connectState = ConnectState.Connected, info = "Connected")
        }
        updatePreferencesUseCase.ble {
            it.copy(lastSelectType = SelectType.Client, device = BleDevice(name, device.address))
        }
    }

    private suspend fun CoroutineScope.handleServer() {
        when (state.value.serverState) {
            ServerState.Active -> {
                serverJob?.cancel()
                _state.update {
                    it.copy(serverState = ServerState.Inactive, info = "Server Inactive")
                }
            }

            else -> {
                _state.update {
                    it.copy(serverState = ServerState.Active, info = "Server Active")
                }
                serverJob?.cancel()
                serverJob = bleServerStartAndReadUseCase().onEach { err ->
                    _state.update {
                        it.copy(
                            serverState = ServerState.Error,
                            info = "Server Error $err"
                        )
                    }
                }.launchIn(this)
                updatePreferencesUseCase.ble {
                    it.copy(lastSelectType = SelectType.Server)
                }
            }
        }
    }

    private fun CoroutineScope.handleAdvertise() {
        when (state.value.advertisingState) {
            AdvertisingState.Running -> {
                advertisingJob?.cancel()
                _state.update {
                    it.copy(
                        advertisingState = AdvertisingState.Stopped,
                        info = "Advertise Stopped"
                    )
                }
            }

            else -> {
                _state.update {
                    it.copy(advertisingState = AdvertisingState.Running, info = "Advertising")
                }
                advertisingJob?.cancel()
                advertisingJob = bleServerStartAdvertisingUseCase().onEach { err ->
                    val state =
                        if (err is BleError.AdvertiseTimeout) _state.value.copy(
                            advertisingState = AdvertisingState.Stopped,
                            info = "Advertise Timeout"
                        )
                        else _state.value.copy(
                            advertisingState = AdvertisingState.Error,
                            info = "Advertise Error ${err.msg}"
                        )
                    _state.update { state }
                    advertisingJob?.cancel()
                }.launchIn(this)
            }
        }
    }

    private suspend fun top(action: TopBarAction) = when (action) {
        TopBarAction.CleanLog -> {
            cleanLogUseCase(Owner.BLE)
        }

        TopBarAction.Close -> {
            advertisingJob?.cancel()
            serverJob?.cancel()
            scanJob?.cancel()
            connectJob?.cancel()
            _state.update {
                it.copy(
                    advertisingState = AdvertisingState.Stopped,
                    scanState = ScanState.Stopped,
                    serverState = ServerState.Inactive,
                    connectState = ConnectState.Disconnected
                )
            }
        }

        is TopBarAction.IsShowSettingDialog -> _state.update {
            it.copy(isShowSettingDialog = action.show)
        }
    }
}