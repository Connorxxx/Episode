package com.connor.episode.data.remote.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.connor.episode.core.utils.TargetApi
import com.connor.episode.domain.model.error.BleError
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BleClient @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bluetoothManager: BluetoothManager
) {

    private val bluetoothAdapter = bluetoothManager.adapter
    private var bluetoothGatt: BluetoothGatt? = null
    private var messageCharacteristic: BluetoothGattCharacteristic? = null

    @SuppressLint("MissingPermission")  //TODO Make sure has Manifest.permission.BLUETOOTH_CONNECT permission
    fun connect(device: BluetoothDevice) = callbackFlow {
        val gattCallback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                when {
                    status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothGatt.STATE_CONNECTED ->
                        gatt?.discoverServices() ?: trySend(BleError.Connect("Failed to discover services").left())
                    else -> trySend(BleError.Connect("Failed to connect to device").left())

                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    val service = gatt.getService(BleConfig.SERVICE_UUID) ?: run {
                        trySend(BleError.Connect("Failed to discover services").left())
                        return
                    }
                    messageCharacteristic = service.getCharacteristic(BleConfig.MESSAGE_CHARACTERISTIC_UUID) ?: run {
                        trySend(BleError.Connect("Failed to discover services").left())
                        return
                    }
                    val enable = gatt.setCharacteristicNotification(messageCharacteristic!!, true)
                    if (!enable) {
                        trySend(BleError.Connect("Failed to discover services").left())
                        return
                    }
                    gatt.writeDescriptorSafely(messageCharacteristic!!).onLeft {
                        trySend(it.left())
                        return
                    }

                } else trySend(BleError.Connect("Failed to discover services").left())
            }

            override fun onCharacteristicChanged(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?  //Android 12 and lower version
            ) {
                characteristic?.let {
                    if (it.uuid == BleConfig.MESSAGE_CHARACTERISTIC_UUID) {
                        val messageBytes = it.value
                        trySend(messageBytes.right())
                    } else trySend(BleError.Connect("Failed to discover services").left())
                } ?: trySend(BleError.Connect("Failed to discover services").left())
            }

            override fun onCharacteristicChanged( //Android 13 and higher version
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray
            ) {
                if (characteristic.uuid == BleConfig.MESSAGE_CHARACTERISTIC_UUID) {
                    trySend(value.right())
                } else trySend(BleError.Connect("Failed to discover services").left())
            }
        }
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
        awaitClose {
            disconnect()
        }
    }.flowOn(Dispatchers.IO).catch {
        emit(BleError.Connect("Failed to connect to device: $it").left())
    }

    @SuppressLint("MissingPermission")  //TODO Make sure has Manifest.permission.BLUETOOTH_SCAN permission
    val scanDevice: Flow<Either<BleError, BluetoothDevice>> = callbackFlow {
        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                result?.device?.let { device ->
//                    val matchesServiceUuid = result.scanRecord?.serviceUuids?.contains(ParcelUuid(BleConfig.SERVICE_UUID)) == true
//                    if (matchesServiceUuid) //判断特定服务
                        trySend(device.right())
                }
            }

            override fun onScanFailed(errorCode: Int) {
                trySend(BleError.Scan("Scan failed with error code: $errorCode").left())
            }
        }

        val scanFilter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(BleConfig.SERVICE_UUID))
            .build()

        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        bluetoothAdapter.bluetoothLeScanner.startScan(
            listOf(),
            scanSettings,
            scanCallback
        )

        awaitClose {
            bluetoothAdapter.bluetoothLeScanner.stopScan(scanCallback)
        }
    }.flowOn(Dispatchers.IO).catch {
        emit(BleError.Scan("Failed to scan device: $it").left())
    }

    @SuppressLint("MissingPermission")  //TODO Make sure has Manifest.permission.BLUETOOTH_CONNECT permission
    suspend fun sendMessage(rawMessage: ByteArray): Either<BleError, Unit> {
        if (rawMessage.size > BleConfig.MAX_MESSAGE_LENGTH) return BleError.Write("Message is too long").left()
        if (bluetoothGatt == null) return BleError.Write("bluetoothGatt is null").left()
        if (messageCharacteristic == null) return BleError.Write("messageCharacteristic is null").left()
        messageCharacteristic!!.value = rawMessage
        return bluetoothGatt!!.writeCharacteristicSafely(messageCharacteristic!!, rawMessage)
    }

    @SuppressLint("MissingPermission")  //TODO Make sure has Manifest.permission.BLUETOOTH_CONNECT permission
    private fun BluetoothGatt.writeDescriptorSafely(
        messageCharacteristic: BluetoothGattCharacteristic,
    ): Either<BleError, Unit> {
        val descriptor =
            messageCharacteristic.getDescriptor(BleConfig.CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID)
        val success = if (TargetApi.T) {
            val result = writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
            result == BluetoothGatt.GATT_SUCCESS
        } else {
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            writeDescriptor(descriptor)
        }
        return if (!success) BleError.Write("Failed to write descriptor").left()
        else Unit.right()
    }

    @SuppressLint("MissingPermission")  //TODO Make sure has Manifest.permission.BLUETOOTH_CONNECT permission
    private suspend fun BluetoothGatt.writeCharacteristicSafely(
        characteristic: BluetoothGattCharacteristic,
        data: ByteArray
    ) = Either.catch {
        val success = withContext(Dispatchers.IO) {
            if (TargetApi.T) {
                val result = writeCharacteristic(
                    characteristic,
                    data,
                    BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                )
                result == BluetoothGatt.GATT_SUCCESS
            } else {
                characteristic.value = data
                writeCharacteristic(characteristic)
            }
        }
        if (!success) return BleError.Write("Failed to write characteristic").left()
    }.mapLeft {
        BleError.Write("Failed to send message: $it")
    }

    @SuppressLint("MissingPermission")  //TODO Make sure has Manifest.permission.BLUETOOTH_CONNECT permission
    private fun disconnect() {
        bluetoothGatt?.apply {
            disconnect()
            close()
        }
        bluetoothGatt = null
        messageCharacteristic = null
    }

}