package com.connor.episode.features.serial

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connor.episode.core.utils.asciiToHexString
import com.connor.episode.core.utils.filterHex
import com.connor.episode.core.utils.hexStringToAscii
import com.connor.episode.core.utils.hexStringToByteArray
import com.connor.episode.core.utils.logCat
import com.connor.episode.data.mapper.toModel
import com.connor.episode.data.mapper.toPreferences
import com.connor.episode.data.mapper.toUiState
import com.connor.episode.data.mapper.updateFromPref
import com.connor.episode.domain.model.Message
import com.connor.episode.domain.model.SerialPortModel
import com.connor.episode.domain.usecase.GetSerialModelUseCase
import com.connor.episode.domain.usecase.OpenReadSerialUseCase
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
    private val openReadSerialUseCase: OpenReadSerialUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SerialPortState())
    val state = _state.asStateFlow()

    private var readJob: Job? = null

    init {
        "ViewModel init ${hashCode()}".logCat()
        viewModelScope.launch {
            getSerialModelUseCase().toUiState().also { state ->
                _state.update { state }
            }
        }
    }

    fun onAction(action: SerialPortAction) {
        when (action) {
            is SerialPortAction.Send -> send(action)

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
                serialPortRepository.close()
            }

            is SerialPortAction.ReceiveFormatSelect -> updatePreferences { preferences ->
                preferences.copy(receiveFormat = action.idx)
            }

            is SerialPortAction.SendFormatSelect -> sendFormatSelect(action)

            SerialPortAction.Resend -> updatePreferences { preferences ->
                preferences.copy(resend = !preferences.resend)
            }

            is SerialPortAction.ResendSeconds -> updatePreferences { preferences ->
                preferences.copy(resendSeconds = action.seconds)
            }

            is SerialPortAction.OnMessageChange -> {
                val msg = if (_state.value.sendFormat == 0) {
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

    private fun sendFormatSelect(action: SerialPortAction.SendFormatSelect) {
        if (_state.value.sendFormat == action.idx) return
        val text = _state.value.message.text
        if (action.idx == 0) {
            val text = text.asciiToHexString()
            _state.update {
                it.copy(
                    message = TextFieldValue(
                        text = text,
                        selection = TextRange(text.length)
                    )
                )
            }
        } else {
            if (text.replace("\\s+".toRegex(), "").length % 2 != 0) return
            val text = text.replace("\\s+".toRegex(), "").hexStringToAscii()
            _state.update {
                it.copy(
                    message = TextFieldValue(
                        text = text,
                        selection = TextRange(text.length)
                    )
                )
            }
        }
        updatePreferences { preferences ->
            preferences.copy(sendFormat = action.idx)
        }
    }

    private fun send(action: SerialPortAction.Send) {
        if (action.msg.isEmpty() || !_state.value.isConnected) return
        val bytesMsg = if (_state.value.sendFormat == 0) action.msg.hexStringToByteArray()
        else action.msg.toByteArray(Charsets.US_ASCII)
        serialPortRepository.write(bytesMsg).fold(
            ifLeft = { err ->
                _state.update {
                    it.copy(extraInfo = err.msg)
                }
            },
            ifRight = {
                _state.update {
                    it.copy(
                        messages = it.messages + Message(action.msg, true)
                    )
                }
            }
        )
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun confirm(action: SerialPortAction.ConfirmSetting) {
        if (_state.value.serialPort == action.serialPort &&
            _state.value.baudRate == action.baudRate &&
            _state.value.isConnected
        ) return
        _state.update { it.copy(showSettingDialog = false) }
        if (readJob?.isActive == true) readJob?.cancel()
        readJob = viewModelScope.launch {
            openReadSerialUseCase(
                model = _state.value.toModel().copy(serialPort = action.serialPort, baudRate = action.baudRate),
            ).collect {
                val state = it.fold(
                    ifLeft = { err -> state.value.copy(isConnected = !err.isFatal, extraInfo = err.msg) },
                    ifRight = { bytes -> state.value.copy(messages = state.value.messages + Message(bytes.toHexString(), false)) }
                )
                _state.update { state }
            }
        }
    }

    private fun updatePreferences(pref: (SerialPortModel) -> SerialPortModel) =
        viewModelScope.launch {
            val pref = serialPortRepository.updatePreferences(pref(_state.value.toModel())).toPreferences()
            _state.update { it.updateFromPref(pref) }
        }
}