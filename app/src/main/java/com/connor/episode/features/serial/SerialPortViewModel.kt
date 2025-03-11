package com.connor.episode.features.serial

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connor.episode.core.utils.asciiToHexString
import com.connor.episode.core.utils.hexStringToAscii
import com.connor.episode.core.utils.logCat
import com.connor.episode.data.mapper.toMessage
import com.connor.episode.domain.model.business.ModelType
import com.connor.episode.domain.model.business.Owner
import com.connor.episode.domain.model.uimodel.BottomBarAction
import com.connor.episode.domain.model.uimodel.SerialPortAction
import com.connor.episode.domain.model.uimodel.SerialPortState
import com.connor.episode.domain.model.uimodel.TopBarAction
import com.connor.episode.domain.usecase.CleanLogUseCase
import com.connor.episode.domain.usecase.CloseConnectUseCase
import com.connor.episode.domain.usecase.GetSerialModelUseCase
import com.connor.episode.domain.usecase.ObserveNewMessageUseCase
import com.connor.episode.domain.usecase.ObservePrefUseCase
import com.connor.episode.domain.usecase.OpenReadSerialUseCase
import com.connor.episode.domain.usecase.ResendUseCase
import com.connor.episode.domain.usecase.SendDataUseCase
import com.connor.episode.domain.usecase.UpdatePreferencesUseCase
import com.connor.episode.domain.usecase.WriteMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SerialPortViewModel @Inject constructor(
    private val getSerialModelUseCase: GetSerialModelUseCase,
    private val openReadSerialUseCase: OpenReadSerialUseCase,
    private val writeMessageUseCase: WriteMessageUseCase,
    private val updatePreferencesUseCase: UpdatePreferencesUseCase,
    private val closeConnectUseCase: CloseConnectUseCase,
    private val resendUseCase: ResendUseCase,
    private val cleanLogUseCase: CleanLogUseCase,
    private val observeNewMessageUseCase: ObserveNewMessageUseCase,
    private val sendDataUseCase: SendDataUseCase,
    private val observePrefUseCase: ObservePrefUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SerialPortState())
    val state = _state.asStateFlow()

    private var readJob: Job? = null
    private var resendJob: Job? = null

    init {
        viewModelScope.launch {
            _state.update { getSerialModelUseCase() }
            observePrefUseCase.serial.onEach {
                "serial settings update: $it".logCat()
                _state.update { state ->
                    state.copy(
                        model = state.model.copy(
                            portName = it.serialPort,
                            baudRate = it.baudRate
                        ),
                        bottomBarSettings = it.settings
                    )
                }
            }.launchIn(this)
            observeNewMessageUseCase(Owner.SerialPort).onEach {
                "SerialPort message update: $it".logCat()
                _state.update { state ->
                    state.copy(messages = state.messages + it.toMessage())
                }
            }.launchIn(this)
        }
    }

    fun onAction(action: SerialPortAction) = viewModelScope.launch {
        when (action) {
            is SerialPortAction.Bottom -> bottom(action.bottom)

            is SerialPortAction.Top -> top(action.top)

            is SerialPortAction.ConfirmSetting -> confirm(action)
        }
    }

    private suspend fun top(action: TopBarAction) = when (action) {
        TopBarAction.CleanLog -> {
            cleanLogUseCase(Owner.SerialPort)
            _state.update {
                it.copy(messages = emptyList())
            }
        }

        TopBarAction.Close -> {
            _state.update { it.copy(isConnected = false, extraInfo = "Closed") }
            readJob?.cancel()
            closeConnectUseCase(ModelType.SerialPort)
        }

        is TopBarAction.IsShowSettingDialog -> _state.update {
            it.copy(showSettingDialog = action.show)
        }
    }

    private suspend fun bottom(action: BottomBarAction) = when (action) {
        is BottomBarAction.Send -> if (_state.value.isConnected && _state.value.message.text.isNotEmpty())
            send(action).let { state ->
                _state.update { state }
            } else Unit

        is BottomBarAction.ReceiveFormatSelect -> updatePreferencesUseCase.serial {
            it.copy(settings = it.settings.copy(receiveFormat = action.idx))
        }

        is BottomBarAction.SendFormatSelect -> sendFormatSelect(action)

        is BottomBarAction.Resend -> {
            resendJob?.cancel()
            resendJob = viewModelScope.launch {
                resendUseCase(action.resend, Owner.SerialPort).collect {
                    _state.value = state.value.copy(extraInfo = it)
                }
            }
        }

        is BottomBarAction.ResendSeconds -> updatePreferencesUseCase.serial {
            it.copy(settings = it.settings.copy(resendSeconds = action.seconds))
        }

        is BottomBarAction.OnMessageChange -> _state.update { state ->
            state.copy(message = action.msg)
        }

        is BottomBarAction.Expand -> _state.update {
            it.copy(expandedBottomBar = action.expand)
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
        updatePreferencesUseCase.serial {
            it.copy(settings = it.settings.copy(sendFormat = action.idx))
        }
    }

    private suspend fun send(action: BottomBarAction.Send) = run {
        val byte = writeMessageUseCase(action.msg, Owner.SerialPort)
        sendDataUseCase(byte, ModelType.SerialPort).fold(
            ifLeft = { _state.value.copy(extraInfo = it) },
            ifRight = {
                _state.value.copy(
                    message = TextFieldValue()
                )
            }
        )
    }

    private fun confirm(action: SerialPortAction.ConfirmSetting) {
        "${action.serialPort} ${action.baudRate}".logCat()
        "${state.value.model.serialPorts}".logCat()
        _state.update {
            it.copy(
                showSettingDialog = false,
                isConnected = true,
                extraInfo = "Connected"
            )
        }
        readJob?.cancel()
        readJob = viewModelScope.launch {
            val path = state.value.model.serialPorts
                .find { it.path.contains(action.serialPort) }?.path ?: return@launch
            "path $path".logCat()
            openReadSerialUseCase {
                devicePath = path
                baudRate = action.baudRate
            }.collect {
                _state.update { state ->
                    state.copy(
                        isConnected = !it.isFatal,
                        extraInfo = it.msg
                    )
                }
            }
        }
    }
}