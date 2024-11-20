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
    private val _messages = MutableStateFlow(emptyList<Message>())
    val state = _state.asStateFlow()

    private var readJob: Job? = null

    init {
        "ViewModel init ${hashCode()}".logCat()
        val list = if (BuildConfig.DEBUG && false) listOf("ttyS0", "ttyS1", "ttyS2", "ttyS3")
        else serialPortRepository.getAllDevices.sortedWith(compareBy({ it.length }, { it }))
        _state.update {
            it.copy(
                serialPorts = list,
                serialPort = list.firstOrNull()?.substringAfterLast("/") ?: ""
            )
        }
    }

    fun onAction(action: SerialPortAction) {
        when (action) {
            is SerialPortAction.Send -> {
                if (action.msg.isEmpty() || !_state.value.isConnected) return
                serialPortRepository.write(byteArrayOf(0x00, 0x01, 0x02, 0x03)).fold(
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

            SerialPortAction.CleanLog -> _state.update {
                it.copy(messages = emptyList())
            }

            SerialPortAction.IsShowSettingDialog -> _state.update {
                it.copy(showSettingDialog = !it.showSettingDialog,)
            }

            is SerialPortAction.ConfirmSetting -> {
                val path =
                    _state.value.serialPorts.find { it.contains(action.serialPort) } ?: return
                _state.update {
                    it.copy(
                        showSettingDialog = false,
                        serialPort = action.serialPort,
                        baudRate = action.baudRate
                    )
                }
                readJob = viewModelScope.open(path, _state.value.baudRate.toInt())
                //if (_state.value.serialPort.isEmpty()) return
            }

            SerialPortAction.Close -> {
                _state.update {
                    it.copy(isConnected = false, extraInfo = "Close")
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