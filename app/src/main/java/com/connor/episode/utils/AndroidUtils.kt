package com.connor.episode.utils

import android.app.Service
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.connor.episode.App
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun Any.logCat(tab: String = "Episode") {
    // if (!BuildConfig.DEBUG) return
    if (this is String)  Log.d(tab, this) else Log.d(tab, this.toString())
}

fun String.showToast() {
    Toast.makeText(App.app, this, Toast.LENGTH_LONG).show()
}

inline fun <reified T> Context.intent(builder: Intent.() -> Unit = {}): Intent =
    Intent(this, T::class.java).apply(builder)

inline fun <reified T : Service> Context.startService(block: Intent.() -> Unit = {}) =
    startForegroundService(intent<T>(block))

fun NavHostController.navigateSingleTopTo(route: String) =
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }

fun NavHostController.navigateTopTo(route: String) =
    navigate(route) {
        popUpTo(0) {
            saveState = true
            inclusive = true
        }
        launchSingleTop = true
        restoreState = true
    }

fun LocalDateTime.formatSmartly(): String {
    val now = LocalDateTime.now()
    val today = LocalDate.now()
    return when {
        this.toLocalDate() == today -> {
            this.format(DateTimeFormatter.ofPattern("'Today' HH:mm"))
        }
        this.year == now.year -> {
            this.format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))
        }
        else -> {
            this.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"))
        }
    }
}