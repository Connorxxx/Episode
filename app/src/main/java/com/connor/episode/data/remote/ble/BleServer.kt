package com.connor.episode.data.remote.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.os.ParcelUuid
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.connor.episode.core.utils.TargetApi
import com.connor.episode.core.utils.logCat
import com.connor.episode.domain.model.error.BleError
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BleServer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bluetoothManager: BluetoothManager
) {

    private val bluetoothAdapter by lazy { bluetoothManager.adapter }
    private var gattServer: BluetoothGattServer? = null
    private var messageCharacteristic: BluetoothGattCharacteristic? = null
    private val connectedDevices = mutableSetOf<BluetoothDevice>()


    @SuppressLint("MissingPermission")  //TODO Make sure has Manifest.permission.BLUETOOTH_CONNECT permission
    val startServerAndRead = callbackFlow {
        val gattServerCallback = object : BluetoothGattServerCallback() {
            override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> connectedDevices.add(device)

                    BluetoothProfile.STATE_DISCONNECTED -> connectedDevices.remove(device)
                }
            }

            override fun onCharacteristicWriteRequest(
                device: BluetoothDevice,
                requestId: Int,
                characteristic: BluetoothGattCharacteristic,
                preparedWrite: Boolean,
                responseNeeded: Boolean,
                offset: Int,
                value: ByteArray
            ) {
                if (characteristic.uuid == BleConfig.MESSAGE_CHARACTERISTIC_UUID) {
                    if (responseNeeded) {
                        gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
                    }
                    trySend((device.address to value).right())
                }
            }
        }

        gattServer = bluetoothManager.openGattServer(context, gattServerCallback).also {
            val success = it.addChatService()
            if (!success) trySend(BleError.Start("Failed to add chat service").left())
        }

        awaitClose {
            gattServer?.apply {
                clearServices()
                close()
            }
            gattServer = null
        }
    }.flowOn(Dispatchers.IO).catch {
        emit(BleError.Start("Failed to start server: $it").left())
    }

    @SuppressLint("MissingPermission")  //TODO Make sure has Manifest.permission.BLUETOOTH_CONNECT permission
    val startAdvertising: Flow<Either<BleError, Unit>> = callbackFlow {
        val advertiseSettings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
            .setConnectable(true)
            .setTimeout(0)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
            .build()

        val advertiseData = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .addServiceUuid(ParcelUuid(BleConfig.SERVICE_UUID))
            .build()

        val advertiseCallback = object : AdvertiseCallback() {
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                trySend(Unit.right())
            }
            override fun onStartFailure(errorCode: Int) {
                trySend(BleError.Advertise("onStartFailure: $errorCode").left())
            }
        }

        val bluetoothLeAdvertiser = bluetoothAdapter.bluetoothLeAdvertiser
        bluetoothLeAdvertiser.startAdvertising(
            advertiseSettings,
            advertiseData,
            advertiseCallback
        )

        awaitClose {
            bluetoothAdapter.bluetoothLeAdvertiser.stopAdvertising(advertiseCallback)
        }
    }.flowOn(Dispatchers.IO).catch {
        emit(BleError.Advertise("Failed to start advertising: $it").left())
    }

    @SuppressLint("MissingPermission")  //TODO Make sure has Manifest.permission.BLUETOOTH_CONNECT permission
    suspend fun sendMessage(rawMessage: ByteArray) = Either.catch {
        if (gattServer == null) return BleError.Write("gattServer is null").left()
        if (connectedDevices.isEmpty()) return BleError.Write("No connected devices").left()
        if (rawMessage.size > BleConfig.MAX_MESSAGE_LENGTH) return BleError.Write("Message is too long").left()
        coroutineScope {
            val results = connectedDevices.map { device ->
                async(Dispatchers.IO) {
                    if (TargetApi.T) {
                        gattServer!!.notifyCharacteristicChanged(device, messageCharacteristic!!, false, rawMessage)
                    } else {
                        messageCharacteristic!!.value = rawMessage
                        gattServer!!.notifyCharacteristicChanged(device, messageCharacteristic!!, false)
                    }
                }
            }.awaitAll()
            val err = results.filterIsInstance<Either.Left<BleError>>()
            "Server send message: ${err.size} failed".logCat()
            if (err.isNotEmpty()) err.first().value.left()
        }
    }.mapLeft {
        BleError.Write("Failed to send message: $it")
    }

    @SuppressLint("MissingPermission")  //TODO Make sure has Manifest.permission.BLUETOOTH_CONNECT permission
    private fun BluetoothGattServer.addChatService(): Boolean {
        val service = BluetoothGattService(
            BleConfig.SERVICE_UUID,
            BluetoothGattService.SERVICE_TYPE_PRIMARY
        )
        messageCharacteristic = BluetoothGattCharacteristic(
            BleConfig.MESSAGE_CHARACTERISTIC_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE or
                    BluetoothGattCharacteristic.PROPERTY_READ or
                    BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            BluetoothGattCharacteristic.PERMISSION_WRITE or
                    BluetoothGattCharacteristic.PERMISSION_READ
        )
        service.addCharacteristic(messageCharacteristic)
        return addService(service)
    }

}