package com.connor.episode.features.common.delegate

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.paging.cachedIn
import com.connor.episode.core.utils.asciiToHexString
import com.connor.episode.core.utils.getLocalIp
import com.connor.episode.core.utils.hexStringToAscii
import com.connor.episode.core.utils.logCat
import com.connor.episode.domain.model.business.ModelType
import com.connor.episode.domain.model.business.NetResult
import com.connor.episode.domain.model.business.Owner
import com.connor.episode.domain.model.business.SelectType
import com.connor.episode.domain.model.uimodel.BottomBarAction
import com.connor.episode.domain.model.uimodel.NetAction
import com.connor.episode.domain.model.uimodel.NetState
import com.connor.episode.domain.model.uimodel.TopBarAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NetworkViewModelDelegate(
    private val owner: Owner,
    private val useCases: NetUseCases,
    private val scope: CoroutineScope
) : NetworkViewModel {

    private val _state = MutableStateFlow(NetState())
    override val state = _state.asStateFlow()

    override val messagePagingFlow = useCases.pagingMessageUseCase(owner).cachedIn(scope)

    private var serverJob: Job? = null
    private var clientJob: Job? = null
    private var resendJob: Job? = null

    private val modelType = when (owner) {
        Owner.TCP -> ModelType.TCPServer to ModelType.TCPClient
        Owner.UDP -> ModelType.UDPServer to ModelType.UDPClient
        Owner.WebSocket -> ModelType.WebSocketServer to ModelType.WebSocketClient
        else -> error("Not support")
    }

    init {
        scope.launch {
            _state.update { useCases.getNetModelUseCase(owner) }
            when (owner) {
                Owner.TCP -> useCases.observePrefUseCase.tcp
                Owner.UDP -> useCases.observePrefUseCase.udp
                Owner.WebSocket -> useCases.observePrefUseCase.webSocket
                else -> error("Not support")
            }.onEach {
                "$owner settings update: $it".logCat()
                _state.update { state ->
                    state.copy(
                        bottomBarSettings = it.settings,
                        currentType = it.lastSelectType
                    )
                }
            }.launchIn(this)
        }
    }

    override fun onAction(action: NetAction) = scope.launch {
        when (action) {
            is NetAction.Bottom -> bottom(action.bottom)
            is NetAction.Top -> top(action.top)
            is NetAction.ConnectServer -> connectRemote(action)
            is NetAction.StartServer -> startServer(action)
        }
    }

    private fun startServer(action: NetAction.StartServer) = scope.launch {
        when (state.value.result) {
            NetResult.Server if (state.value.model.server.port == action.port.toInt()) -> return@launch
            NetResult.Client -> {
                clientJob?.cancel()
                useCases.closeConnectUseCase(modelType.second)
            }

            else -> Unit
        }
        _state.update {
            it.copy(
                model = it.model.copy(
                    server = it.model.server.copy(
                        localIp = getLocalIp() ?: "network error",
                        port = action.port.toInt()
                    )
                ),
                result = NetResult.Server
            )
        }
        serverJob?.cancel()
        useCases.closeConnectUseCase(modelType.first)
        serverJob = useCases.startServerUseCase(action.port.toInt(), owner).onEach { err ->
            if (err.isFatal) {
                _state.update {
                    it.copy(result = NetResult.Error, error = err.msg)
                }
                useCases.closeConnectUseCase(modelType.first)
                cancel()
            } else "A non-fatal error：$err".logCat()
        }.launchIn(this)
    }

    private fun connectRemote(action: NetAction.ConnectServer) = scope.launch {
        when (state.value.result) {
            NetResult.Client if (state.value.model.client.ip == action.ip && state.value.model.client.port == action.port.toInt()) -> return@launch
            NetResult.Server -> {
                serverJob?.cancel()
                useCases.closeConnectUseCase(modelType.first)
            }

            else -> Unit
        }
        _state.update {
            it.copy(
                model = it.model.copy(
                    client = it.model.client.copy(
                        ip = action.ip,
                        port = action.port.toInt()
                    )
                ),
                result = NetResult.Client
            )
        }
        clientJob?.cancel()
        useCases.closeConnectUseCase(modelType.second)
        clientJob = useCases.connectTCPServerUseCase(action.ip, action.port.toInt(), owner).onEach { err ->
            _state.update {
                it.copy(result = NetResult.Error, error = err.msg)
            }
            useCases.closeConnectUseCase(modelType.second)
            cancel()
        }.launchIn(this)
    }

    private suspend fun top(action: TopBarAction) = when (action) {
        TopBarAction.CleanLog -> {
            useCases.cleanLogUseCase(owner)
        }

        TopBarAction.Close -> close()

        is TopBarAction.IsShowSettingDialog -> _state.update {
            it.copy(isShowSettingDialog = action.show)
        }
    }

    private suspend fun bottom(action: BottomBarAction) {
        when (action) {
            is BottomBarAction.Send -> {
                if (
                    (state.value.result != NetResult.Error ||
                            state.value.result != NetResult.Close) &&
                    state.value.message.text.isEmpty()
                ) Unit
                else _state.update { send(action) }
            }

            is BottomBarAction.Expand -> _state.update {
                it.copy(expandedBottomBar = action.expand)
            }

            is BottomBarAction.OnMessageChange -> _state.update { state ->
                state.copy(message = action.msg)
            }

            is BottomBarAction.ReceiveFormatSelect -> when(owner) {
                Owner.TCP -> useCases.updatePreferencesUseCase.tcp {
                    it.copy(settings = it.settings.copy(receiveFormat = action.idx))
                }
                Owner.UDP -> useCases.updatePreferencesUseCase.udp {
                    it.copy(settings = it.settings.copy(receiveFormat = action.idx))
                }
                Owner.WebSocket -> useCases.updatePreferencesUseCase.webSocket {
                    it.copy(settings = it.settings.copy(receiveFormat = action.idx))
                }
                else -> error("not support")
            }
            is BottomBarAction.SendFormatSelect -> sendFormatSelect(action)
            is BottomBarAction.Resend -> {
                resendJob?.cancel()
                resendJob = scope.launch {
                    useCases.resendUseCase(action.resend, owner).collect {
                        _state.value = state.value.copy(error = it)
                    }
                }
            }
            is BottomBarAction.ResendSeconds -> when (owner) {
                Owner.TCP -> useCases.updatePreferencesUseCase.tcp {
                    it.copy(settings = it.settings.copy(resendSeconds = action.seconds))
                }
                Owner.UDP -> useCases.updatePreferencesUseCase.udp {
                    it.copy(settings = it.settings.copy(resendSeconds = action.seconds))
                }
                Owner.WebSocket -> useCases.updatePreferencesUseCase.webSocket {
                    it.copy(settings = it.settings.copy(resendSeconds = action.seconds))
                }
                else -> error("not support")
            }
        }
    }


    private suspend fun send(action: BottomBarAction.Send) = run {
        val byte = useCases.writeMessageUseCase(action.msg, owner)
        when (state.value.currentType) {
            SelectType.Server -> useCases.sendDataUseCase(action.msg, modelType.first, owner)
            SelectType.Client -> useCases.sendDataUseCase(action.msg, modelType.second, owner)
        }.fold(
            ifLeft = { state.value.copy(result = NetResult.Error, error = it) },
            ifRight = { state.value.copy(message = TextFieldValue()) }
        )
    }

    private suspend fun close() {
        when (state.value.result) {
            NetResult.Server -> {
                serverJob?.cancel()
                useCases.closeConnectUseCase(modelType.first)
            }

            NetResult.Client -> {
                clientJob?.cancel()
                useCases.closeConnectUseCase(modelType.second)
            }

            else -> Unit
        }
        _state.update {
            it.copy(result = NetResult.Close)
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
        when (owner) {
            Owner.TCP -> useCases.updatePreferencesUseCase.tcp {
                it.copy(settings = it.settings.copy(sendFormat = action.idx))
            }
            Owner.UDP -> useCases.updatePreferencesUseCase.udp {
                it.copy(settings = it.settings.copy(sendFormat = action.idx))
            }
            Owner.WebSocket -> useCases.updatePreferencesUseCase.webSocket {
                it.copy(settings = it.settings.copy(sendFormat = action.idx))
            }
            else -> error("not support")
        }
    }
}