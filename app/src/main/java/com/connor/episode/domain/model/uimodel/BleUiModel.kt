package com.connor.episode.domain.model.uimodel

import com.connor.episode.domain.model.business.SelectType

data class BleState(
    val advertisingState: AdvertisingState = AdvertisingState.Stopped,
    val serverState: ServerState = ServerState.Inactive,
    val scanState: ScanState = ScanState.Stopped,
    val connectState: ConnectState = ConnectState.Disconnected,
    val currentType: SelectType = SelectType.Server,
    val isShowSettingDialog: Boolean = false,
    val info: String = "",

    val connect: String = "",
    val scan: String = "",
    val advertising: String = "",
    val startServer: String = "",
)

sealed interface BleAction {
    data class Top(val top: TopBarAction) : BleAction
    data class UpdateSelectType(val i: Int) : BleAction
    //data class Bottom(val bottom: BottomBarAction) : BleAction
    //data class Top(val top: TopBarAction) : BleAction
    data object StartAdvertising : BleAction
    data object StartServer : BleAction
    data object StartScanning : BleAction
    data object TestConnect : BleAction
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