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
import com.connor.episode.domain.model.error.BluetoothError
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
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
                        gatt?.discoverServices() ?: trySend(BluetoothError.Connect("Failed to discover services").left())
                    else -> trySend(BluetoothError.Connect("Failed to connect to device").left())

                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    val service = gatt?.getService(BleConfig.SERVICE_UUID) ?: run {
                        trySend(BluetoothError.Connect("Failed to discover services").left())
                        return
                    }
                    messageCharacteristic = service.getCharacteristic(BleConfig.MESSAGE_CHARACTERISTIC_UUID) ?: run {
                        trySend(BluetoothError.Connect("Failed to discover services").left())
                        return
                    }
                    val enable = gatt.setCharacteristicNotification(messageCharacteristic!!, true)
                    if (!enable) {
                        trySend(BluetoothError.Connect("Failed to discover services").left())
                        return
                    }
                    gatt.writeDescriptorSafely(messageCharacteristic!!).onLeft {
                        trySend(it.left())
                        return
                    }

                } else trySend(BluetoothError.Connect("Failed to discover services").left())
            }

            override fun onCharacteristicChanged(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?  //Android 12 and lower version
            ) {
                characteristic?.let {
                    if (it.uuid == BleConfig.MESSAGE_CHARACTERISTIC_UUID) {
                        val messageBytes = it.value
                        val message = String(messageBytes)
                        trySend(message.right())
                    } else trySend(BluetoothError.Connect("Failed to discover services").left())
                } ?: trySend(BluetoothError.Connect("Failed to discover services").left())
            }

            override fun onCharacteristicChanged( //Android 13 and higher version
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray
            ) {
                if (characteristic.uuid == BleConfig.MESSAGE_CHARACTERISTIC_UUID) {
                    val message = String(value)
                    trySend(message.right())
                } else trySend(BluetoothError.Connect("Failed to discover services").left())
            }
        }
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
        awaitClose {
            disconnect()
        }
    }.flowOn(Dispatchers.IO)

    @SuppressLint("MissingPermission")  //TODO Make sure has Manifest.permission.BLUETOOTH_SCAN permission
    val scanDevice = callbackFlow {
        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                result?.device?.let { device ->
                    val matchesServiceUuid = result.scanRecord?.serviceUuids?.contains(ParcelUuid(BleConfig.SERVICE_UUID)) == true
                    if (matchesServiceUuid) trySend(device.right())
                }
            }

            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)
                trySend(BluetoothError.Scan("Scan failed with error code: $errorCode").left())
                close()
            }
        }

        val scanFilter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(BleConfig.SERVICE_UUID))
            .build()

        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        bluetoothAdapter.bluetoothLeScanner.startScan(
            listOf(scanFilter),
            scanSettings,
            scanCallback
        )

        awaitClose {
            bluetoothAdapter.bluetoothLeScanner.stopScan(scanCallback)
        }
    }.flowOn(Dispatchers.IO)

    @SuppressLint("MissingPermission")  //TODO Make sure has Manifest.permission.BLUETOOTH_CONNECT permission
    suspend fun sendMessage(message: String): Either<BluetoothError, Unit> {
        val messageBytes = message.toByteArray()
        if (messageBytes.size > BleConfig.MAX_MESSAGE_LENGTH) return BluetoothError.Write("Message is too long").left()
        if (bluetoothGatt == null) return BluetoothError.Write("bluetoothGatt is null").left()
        if (messageCharacteristic == null) return BluetoothError.Write("messageCharacteristic is null").left()
        messageCharacteristic!!.value = messageBytes
        return bluetoothGatt!!.writeCharacteristicSafely(messageCharacteristic!!, messageBytes)
    }

    @SuppressLint("MissingPermission")  //TODO Make sure has Manifest.permission.BLUETOOTH_CONNECT permission
    private fun BluetoothGatt.writeDescriptorSafely(
        messageCharacteristic: BluetoothGattCharacteristic,
    ): Either<BluetoothError, Unit> {
        val descriptor =
            messageCharacteristic.getDescriptor(BleConfig.CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID)
        val success = if (TargetApi.T) {
            val result = writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
            result == BluetoothGatt.GATT_SUCCESS
        } else {
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            writeDescriptor(descriptor)
        }
        return if (!success) BluetoothError.Write("Failed to write descriptor").left()
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
        if (!success) return BluetoothError.Write("Failed to write characteristic").left()
    }.mapLeft {
        BluetoothError.Write("Failed to send message: $it")
    }

    @SuppressLint("MissingPermission")  //TODO Make sure has Manifest.permission.BLUETOOTH_CONNECT permission
    fun disconnect() {
        bluetoothGatt?.apply {
            disconnect()
            close()
        }
        bluetoothGatt = null
        messageCharacteristic = null
    }

}