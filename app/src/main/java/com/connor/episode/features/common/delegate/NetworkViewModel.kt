package com.connor.episode.features.common.delegate

import com.connor.episode.domain.model.uimodel.NetAction
import com.connor.episode.domain.model.uimodel.NetState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow

interface NetworkViewModel {
    val state: StateFlow<NetState>
    fun onAction(action: NetAction): Job
}