package com.connor.episode.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connor.episode.BuildConfig
import com.connor.episode.errors.SerialPortError
import com.connor.episode.models.Message
import com.connor.episode.models.SerialPortAction
import com.connor.episode.models.SerialPortState
import com.connor.episode.repositorys.SerialPortRepository
import com.connor.episode.utils.logCat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SerialPortViewModel @Inject constructor(
    private val serialPortRepository: SerialPortRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SerialPortState())
    val state = _state.asStateFlow()

    private var readJob: Job? = null

    init {
        "ViewModel init ${hashCode()}".logCat()
        val list = if (BuildConfig.DEBUG && false) listOf("ttyS0", "ttyS1", "ttyS2", "ttyS3")
        else serialPortRepository.getAllDevices.map {
            it.replace("\\s\\(.*?\\)".toRegex(), "")
        }.sortedWith(compareBy({ it.length }, { it }))
        _state.update { it.copy(serialPorts = list, serialPort = list.firstOrNull() ?: "") }
    }

    fun onAction(action: SerialPortAction) {
        when (action) {
            SerialPortAction.Send -> {
                if (_state.value.message.isEmpty() || !_state.value.isConnected) return
                serialPortRepository.write(byteArrayOf(0x00, 0x01, 0x02, 0x03)).fold(
                    ifLeft = { err ->
                        _state.update {
                            it.copy(extraInfo = err.msg)
                        }
                    },
                    ifRight = {
                        _state.update {
                            it.copy(
                                messages = it.messages + Message(it.message, true),
                                message = "",
                                extraInfo = ""
                            )
                        }
                    }
                )
            }

            is SerialPortAction.WriteMsg -> _state.update { it.copy(message = action.msg) }
            is SerialPortAction.IsShowMenu -> _state.update { it.copy(showMenu = action.show) }
            SerialPortAction.CleanLog -> _state.update {
                it.copy(
                    messages = emptyList(),
                    showMenu = false
                )
            }

            SerialPortAction.IsShowSetting -> _state.update { it.copy(showSetting = !it.showSetting, showMenu = false) }
            is SerialPortAction.SelectSerialPort -> _state.update {
                it.copy(serialPort = action.path)
            }

            is SerialPortAction.ChangeBaudRate -> _state.update {
                it.copy(baudRate = action.baudRate)
            }

            SerialPortAction.ConfirmSetting -> {
                readJob = viewModelScope.open(_state.value.serialPort, _state.value.baudRate.toInt())
                if (_state.value.serialPort.isEmpty()) return
                _state.update { it.copy(showSetting = false, showMenu = false) }
            }

            SerialPortAction.Close -> {
                _state.update{
                    it.copy(isConnected = false, extraInfo = "Close", showMenu = false)
                }
                readJob?.cancel()
                serialPortRepository.close()
            }
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun CoroutineScope.open(path: String = "/dev/ttyS0", baudRate: Int = 9600) = launch {
        _state.update { it.copy(isConnected = true, extraInfo = "Connected") }
        serialPortRepository.openAndRead(path, baudRate).collect { reader ->
            reader.fold(
                ifLeft = { err ->
                    err.msg.logCat()
                    when (err) {
                        is SerialPortError.Open,
                        is SerialPortError.Read.EndOfStream,
                        is SerialPortError.Read.IO -> {
                            _state.update { it.copy(isConnected = false, extraInfo = err.msg) }
                            serialPortRepository.close()
                            cancel()
                        }

                        else -> _state.update { it.copy(extraInfo = err.msg) }
                    }
                },
                ifRight = {
                    _state.update { state ->
                        state.copy(messages = state.messages + Message(it.toHexString(), false))
                    }
                }
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        "ViewModel onCleared".logCat()
    }
}