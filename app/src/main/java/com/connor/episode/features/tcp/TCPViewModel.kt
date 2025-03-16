package com.connor.episode.features.tcp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connor.episode.domain.model.business.Owner
import com.connor.episode.domain.model.uimodel.NetAction
import com.connor.episode.features.common.delegate.NetUseCases
import com.connor.episode.features.common.delegate.NetworkViewModel
import com.connor.episode.features.common.delegate.NetworkViewModelDelegate
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TCPViewModel @Inject constructor(
    private val netUseCases: NetUseCases
) : ViewModel(), NetworkViewModel {

    private val delegate by lazy {
        NetworkViewModelDelegate(Owner.TCP, netUseCases, viewModelScope)
    }

    override val state get() = delegate.state
    override val messagePagingFlow get() = delegate.messagePagingFlow
    override fun onAction(action: NetAction) = delegate.onAction(action)


}