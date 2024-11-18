package com.connor.episode.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connor.episode.BuildConfig
import com.connor.episode.models.Message
import com.connor.episode.models.SerialPortAction
import com.connor.episode.models.SerialPortState
import com.connor.episode.repositorys.SerialPortRepository
import com.connor.episode.utils.logCat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
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
        val list = if (BuildConfig.DEBUG) listOf("ttyS0", "ttyS1", "ttyS2", "ttyS3")
        else serialPortRepository.getAllDevices.map {
            it.replace("\\s\\(.*?\\)".toRegex(), "")
        }.sortedWith(compareBy({ it.length }, { it }))
        _state.update { it.copy(serialPorts = list, serialPort = list.first()) }
    }

    fun onAction(action: SerialPortAction) {
        when (action) {
            is SerialPortAction.Send -> {
                if (_state.value.message.isEmpty() && !_state.value.isConnected) return
                _state.update {
                    it.copy(
                        messages = it.messages + Message(it.message, true),
                        message = ""
                    )
                }
                serialPortRepository.write(byteArrayOf(0x00, 0x01, 0x02, 0x03))  //TODO: Test
            }

            is SerialPortAction.WriteMsg -> _state.update { it.copy(message = action.msg) }
            SerialPortAction.ShowMenu -> _state.update { it.copy(showMenu = !it.showMenu) }
            SerialPortAction.CleanLog -> _state.update {
                it.copy(
                    messages = emptyList(),
                    showMenu = false
                )
            }

            SerialPortAction.ShowSetting -> _state.update { it.copy(showSetting = !it.showSetting) }
            is SerialPortAction.SelectSerialPort -> _state.update {
                it.copy(serialPort = action.path)
            }

            is SerialPortAction.ChangeBaudRate -> _state.update {
                it.copy(baudRate = action.baudRate)
            }

            SerialPortAction.ConfirmSetting -> {
                readJob = viewModelScope.open(_state.value.serialPort, _state.value.baudRate.toInt())
                _state.update { it.copy(showSetting = !it.showSetting) }
            }
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun CoroutineScope.open(path: String = "/dev/ttyS0", baudRate: Int = 9600) = launch {
        val serialPortFlow = serialPortRepository.open(path, baudRate)
        _state.update { it.copy(isConnected = serialPortFlow.isRight()) }
        if (serialPortFlow.isLeft()) return@launch
        serialPortFlow.getOrNull()!!.onCompletion {
            serialPortRepository.close()
            _state.update { it.copy(isConnected = false) }
        }.onEach { readFlow ->
            readFlow.fold(
                ifLeft = { err ->
                    err.msg.logCat()
                    _state.update { it.copy(isConnected = false) }
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