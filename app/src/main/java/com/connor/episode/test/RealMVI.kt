package com.connor.episode.test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// 我们的 DSL Scope 依然保持纯净，只负责声明动作
interface CheckoutScope {
    suspend fun getCart(): Either<String, List<Item>>
    suspend fun calculateTotal(items: List<Item>): Int
    suspend fun charge(amount: Int): Int
    suspend fun track(event: String)
}

data class Item(
    val name: String = "",
    val price: Int = 0
)

data class CheckoutState(
    val cartItems: List<Item> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isCheckoutComplete: Boolean = false
) {
    val totalPrice get() = cartItems.sumOf { it.price }
}

sealed interface CheckoutEvent {

    sealed interface Action : CheckoutEvent
    sealed interface Result : CheckoutEvent

    // 用户意图
    data object LoadCart : Action
    data object ClickCheckout : Action

    // 系统反馈 (副作用的结果)
    data class CartLoaded(val items: List<Item>) : Result
    data class CheckoutSuccess(val txId: String) : Result
    data class CheckoutFailed(val reason: String) : Result
    data object LoadingStarted : Result
}

sealed interface CheckoutEffect {
    data class ShowToast(val message: String) : CheckoutEffect
    data class NavigateToSuccess(val orderId: String) : CheckoutEffect
    data object VibrateDevice : CheckoutEffect
}

// 纯函数：输入 State + Event -> 输出 State
// 极其容易测试，无需 Mock
val checkoutReducer: (CheckoutState, CheckoutEvent) -> CheckoutState = { current, event ->
    when (event) {
        is CheckoutEvent.LoadCart,
        is CheckoutEvent.ClickCheckout -> current.copy(
            isLoading = true,
            error = null
        )

        is CheckoutEvent.LoadingStarted -> current.copy(isLoading = true)

        is CheckoutEvent.CartLoaded -> current.copy(
            isLoading = false,
            cartItems = event.items
        )

        is CheckoutEvent.CheckoutSuccess -> current.copy(
            isLoading = false,
            isCheckoutComplete = true,
            cartItems = emptyList() // 结账成功清空购物车
        )

        is CheckoutEvent.CheckoutFailed -> current.copy(
            isLoading = false,
            error = event.reason
        )
    }
}

val effectMapper: (CheckoutEvent) -> List<CheckoutEffect> = { event ->
    when (event) {
        is CheckoutEvent.CheckoutSuccess -> listOf(
            CheckoutEffect.ShowToast("支付成功"),
            CheckoutEffect.NavigateToSuccess(event.txId),
            CheckoutEffect.VibrateDevice
        )
        is CheckoutEvent.CheckoutFailed -> listOf(
            CheckoutEffect.ShowToast("支付失败: ${event.reason}")
        )
        is CheckoutEvent.CartLoaded -> if (event.items.isEmpty()) listOf(
        CheckoutEffect.ShowToast("购物车为空")) else emptyList()
        else -> emptyList()
    }
}

class ViewModel(
    private val interpreter: CheckoutScope  //实际需要使用hilt自动注入
) : ViewModel() {
    // 1. 事件源：不管是UI点击，还是后台结果，都汇入这里
    private val _events = MutableSharedFlow<CheckoutEvent>()

    val state = _events.scan(CheckoutState(), checkoutReducer).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CheckoutState()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val effects = _events.flatMapConcat {
        effectMapper(it).asFlow()
    }.shareIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily
    )

    fun dispatch(event: CheckoutEvent.Action) = viewModelScope.launch {
        // 1. 先让 UI 响应 (比如显示 Loading)
        _events.emit(event)
        handleSideEffect(event)
    }

    private suspend fun handleSideEffect(event: CheckoutEvent.Action) {
        when (event) {
            is CheckoutEvent.LoadCart -> interpreter.getCart().fold(
                ifLeft = { _events.emit(CheckoutEvent.CheckoutFailed(it)) },
                ifRight = { _events.emit(CheckoutEvent.CartLoaded(it)) }
            )
            is CheckoutEvent.ClickCheckout -> interpreter.charge(state.value.totalPrice)
        }
    }
}