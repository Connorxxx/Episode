package com.connor.episode.features.serial

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connor.episode.core.utils.asciiToHexString
import com.connor.episode.core.utils.filterHex
import com.connor.episode.core.utils.hexStringToAscii
import com.connor.episode.domain.model.business.Message
import com.connor.episode.domain.model.uimodel.SerialPortAction
import com.connor.episode.domain.model.uimodel.SerialPortState
import com.connor.episode.domain.usecase.CloseConnectUseCase
import com.connor.episode.domain.usecase.GetSerialModelUseCase
import com.connor.episode.domain.usecase.ObservePrefUseCase
import com.connor.episode.domain.usecase.OpenReadSerialUseCase
import com.connor.episode.domain.usecase.UpdateSerialUseCase
import com.connor.episode.domain.usecase.WriteMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SerialPortViewModel @Inject constructor(
    private val getSerialModelUseCase: GetSerialModelUseCase,
    private val openReadSerialUseCase: OpenReadSerialUseCase,
    private val observePrefUseCase: ObservePrefUseCase,
    private val writeMessageUseCase: WriteMessageUseCase,
    private val updateSerialUseCase: UpdateSerialUseCase,
    private val closeConnectUseCase: CloseConnectUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SerialPortState())
    val state = _state.asStateFlow()

    private var readJob: Job? = null

    init {
        viewModelScope.launch {
            _state.update { getSerialModelUseCase() }
            observePrefUseCase().collect {
                _state.update { state -> state.copy(settings = it.settings) }
            }
        }
    }

    fun onAction(action: SerialPortAction) = viewModelScope.launch {
        when (action) {
            is SerialPortAction.Send -> send(action).let { _state::update }

            SerialPortAction.CleanLog -> _state.update {
                it.copy(messages = emptyList())
            }

            SerialPortAction.IsShowSettingDialog -> _state.update {
                it.copy(showSettingDialog = !it.showSettingDialog)
            }

            is SerialPortAction.ConfirmSetting -> confirm(action)

            SerialPortAction.Close -> {
                _state.update { it.copy(isConnected = false, extraInfo = "Closed") }
                readJob?.cancel()
                closeConnectUseCase()
            }

            is SerialPortAction.ReceiveFormatSelect -> updateSerialUseCase {
                it.copy(settings = it.settings.copy(receiveFormat = action.idx))
            }

            is SerialPortAction.SendFormatSelect -> sendFormatSelect(action)

            SerialPortAction.Resend -> updateSerialUseCase {
                it.copy(settings = it.settings.copy(resend = !it.settings.resend))
            }

            is SerialPortAction.ResendSeconds -> updateSerialUseCase {
                it.copy(settings = it.settings.copy(resendSeconds = action.seconds))
            }

            is SerialPortAction.OnMessageChange -> {
                val msg = if (_state.value.settings.sendFormat == 0) {
                    val text = filterHex(_state.value.message.text, action.msg.text)
                    TextFieldValue(
                        text = text,
                        selection = TextRange(text.length)
                    )
                } else action.msg
                _state.update { state ->
                    state.copy(message = msg)
                }
            }
        }
    }

    private suspend fun sendFormatSelect(action: SerialPortAction.SendFormatSelect) {
        if (_state.value.settings.sendFormat == action.idx) return
        val text = _state.value.message.text
        val state = if (action.idx == 0) {
            val hex = text.asciiToHexString()
            _state.value.copy(message = TextFieldValue(text = hex, selection = TextRange(hex.length)))
        } else {
            if (text.replace(" ", "").length % 2 != 0) return
            val ascii = text.replace(" ", "").hexStringToAscii()
            _state.value.copy(
                    message = TextFieldValue(text = ascii, selection = TextRange(ascii.length))
                )

        }
        _state.update { state }
        updateSerialUseCase {
            it.copy(settings = it.settings.copy(sendFormat = action.idx))
        }
    }

    private suspend fun send(action: SerialPortAction.Send) = writeMessageUseCase(action.msg).fold(
        ifLeft = { _state.value.copy(extraInfo = it) },
        ifRight = { _state.value.copy(messages = _state.value.messages + Message(action.msg, true)) }
    )

    @OptIn(ExperimentalStdlibApi::class)
    private fun confirm(action: SerialPortAction.ConfirmSetting) {
        _state.update { it.copy(showSettingDialog = false) }
        readJob?.cancel()
        readJob = viewModelScope.launch {
            val path =
                state.value.model.serialPorts.find { it.name == action.serialPort }?.path ?: return@launch
            openReadSerialUseCase {
                devicePath = path
                baudRate = action.baudRate
            }.collect {
                val state = it.fold(
                    ifLeft = { err -> state.value.copy(isConnected = !err.isFatal, extraInfo = err.msg) },
                    ifRight = { bytes ->
                        state.value.copy(messages = state.value.messages + Message(bytes.toHexString(), false))
                    }
                )
                _state.update { state }
            }
        }
    }
}