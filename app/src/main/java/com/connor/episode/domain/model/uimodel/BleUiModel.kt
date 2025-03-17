package com.connor.episode.domain.model.uimodel

import androidx.compose.ui.text.input.TextFieldValue
import com.connor.episode.domain.model.business.SelectType
import com.connor.episode.domain.model.preference.BleDevice
import com.connor.episode.domain.model.preference.BottomBarSettings

data class BleState(
    val bluetoothEnabled: Boolean = false,  //TODO Check Bluetooth Enabled
    val advertisingState: AdvertisingState = AdvertisingState.Stopped,
    val serverState: ServerState = ServerState.Inactive,
    val scanState: ScanState = ScanState.Stopped,
    val connectState: ConnectState = ConnectState.Disconnected,
    val currentType: SelectType = SelectType.Server,
    val isShowSettingDialog: Boolean = false,
    val info: String = "Close",
    val deviceNames: Set<String> = emptySet(),
    val connectDevice: BleDevice = BleDevice(),
    val expandedBottomBar: Boolean = false,
    val bottomBarSettings: BottomBarSettings = BottomBarSettings(),
    val message: TextFieldValue = TextFieldValue(""),
)

sealed interface BleAction {
    data class Top(val top: TopBarAction) : BleAction
    data object ClickAdvertise : BleAction
    data object ClickServer : BleAction
    data object ClickScan : BleAction
    data class ClickConnect(val name: String) : BleAction
    data object ClickLink : BleAction
    data class Bottom(val bottom: BottomBarAction) : BleAction
}

enum class AdvertisingState {
    Running, Stopped, Error
}

enum class ServerState {
    Active, Inactive, Error
}

enum class ScanState {
    Scanning, Stopped, Error
}

enum class ConnectState {
    Connected, Disconnected, Error
}