package com.connor.episode.features.common.delegate

import androidx.paging.PagingData
import com.connor.episode.domain.model.business.Message
import com.connor.episode.domain.model.uimodel.NetAction
import com.connor.episode.domain.model.uimodel.NetState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface NetworkViewModel {
    val state: StateFlow<NetState>
    val messagePagingFlow: Flow<PagingData<Message>>
    fun onAction(action: NetAction): Job
}