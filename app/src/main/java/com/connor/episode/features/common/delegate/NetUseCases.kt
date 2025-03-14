package com.connor.episode.features.common.delegate

import com.connor.episode.domain.usecase.CleanLogUseCase
import com.connor.episode.domain.usecase.CloseConnectUseCase
import com.connor.episode.domain.usecase.ConnectServerUseCase
import com.connor.episode.domain.usecase.GetNetModelUseCase
import com.connor.episode.domain.usecase.GetPagingMessageUseCase
import com.connor.episode.domain.usecase.ObservePrefUseCase
import com.connor.episode.domain.usecase.ResendUseCase
import com.connor.episode.domain.usecase.SendDataUseCase
import com.connor.episode.domain.usecase.StartServerUseCase
import com.connor.episode.domain.usecase.UpdatePreferencesUseCase
import com.connor.episode.domain.usecase.WriteMessageUseCase
import javax.inject.Inject

class NetUseCases @Inject constructor(
    val getNetModelUseCase: GetNetModelUseCase,
    val connectTCPServerUseCase: ConnectServerUseCase,
    val closeConnectUseCase: CloseConnectUseCase,
    val writeMessageUseCase: WriteMessageUseCase,
    val cleanLogUseCase: CleanLogUseCase,
    val resendUseCase: ResendUseCase,
    val sendDataUseCase: SendDataUseCase,
    val updatePreferencesUseCase: UpdatePreferencesUseCase,
    val observePrefUseCase: ObservePrefUseCase,
    val startServerUseCase: StartServerUseCase,
    val pagingMessageUseCase: GetPagingMessageUseCase
) {
}