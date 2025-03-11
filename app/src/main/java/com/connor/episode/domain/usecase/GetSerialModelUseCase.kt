package com.connor.episode.domain.usecase

import com.connor.episode.domain.model.business.Owner
import com.connor.episode.domain.model.business.SerialPortModel
import com.connor.episode.domain.model.uimodel.SerialPortState
import com.connor.episode.domain.repository.MessageRepository
import com.connor.episode.domain.repository.PreferencesRepository
import com.connor.episode.domain.repository.SerialPortRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetSerialModelUseCase @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val serialPortRepository: SerialPortRepository,
    private val messageRepository: MessageRepository
) {

    suspend operator fun invoke(): SerialPortState {
        val list = serialPortRepository.getAllDevices
        val serialPref = preferencesRepository.serialPrefFlow.first()
        val messages = messageRepository.getAllMessages(Owner.SerialPort)
        return SerialPortState(
            model = SerialPortModel(
                serialPorts = list,
                portName = serialPref.serialPort.takeIf { it.isNotEmpty() } ?: list.firstOrNull()?.name ?: "",
                baudRate = serialPref.baudRate
            ),
            messages = messages,
            bottomBarSettings = serialPref.settings,
            extraInfo = if (list.isEmpty()) "No Serial Ports Found" else "Closed"
        )
    }
}