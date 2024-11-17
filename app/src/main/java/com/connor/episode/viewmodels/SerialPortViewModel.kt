package com.connor.episode.viewmodels

import androidx.lifecycle.ViewModel
import com.connor.episode.BuildConfig
import com.connor.episode.models.Message
import com.connor.episode.models.SerialPortAction
import com.connor.episode.models.SerialPortState
import com.connor.episode.repositorys.SerialPortRepository
import com.connor.episode.utils.logCat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SerialPortViewModel @Inject constructor(
    private val serialPortRepository: SerialPortRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SerialPortState())
    val state = _state.asStateFlow()

    init {
        val list = if (BuildConfig.DEBUG) listOf("ttyS0", "ttyS1", "ttyS2", "ttyS3")
        else serialPortRepository.getAllDevices.map {
            it.replace("\\s\\(.*?\\)".toRegex(), "")
        }.sortedWith(compareBy({ it.length }, { it }))
        _state.update { it.copy(serialPorts = list, serialPort = list.first()) }
    }

    fun onAction(action: SerialPortAction) {
        when (action) {
            is SerialPortAction.Open -> {
                val isConnected = open(action.path, action.baudRate)
                _state.update { it.copy(isConnected = isConnected) }
            }

            is SerialPortAction.Send -> {
                if (_state.value.message.isEmpty()) return
                _state.update {
                    it.copy(
                        messages = it.messages + Message(it.message, true),
                        message = ""
                    )
                }
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

            SerialPortAction.ConfirmSetting -> _state.update { it.copy(showSetting = !it.showSetting) }
        }
    }

    private fun open(path: String = "/dev/ttyS0", baudRate: Int = 9600) =
        serialPortRepository.open(path, baudRate).fold(
            ifLeft = {
                it.msg.logCat()
                false
            },
            ifRight = { true }
        ).also { open ->
            _state.update { it.copy(isConnected = open) }
        }


}