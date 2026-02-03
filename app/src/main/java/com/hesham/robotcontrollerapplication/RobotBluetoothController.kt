// RobotBluetoothController.kt
package com.example.robotcontroller

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import com.hesham.robotcontrollerapplication.RobotActions
import com.hesham.robotcontrollerapplication.RobotSpeed
import com.hesham.robotcontrollerapplication.UiEvents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.OutputStream
import java.util.UUID

class RobotBluetoothController(
    private val context: Context
) {

    companion object {
        private const val ROBOT_NAME =
            "OTHERLOGIC" // This is my laptop Bluetooth name , this for testing my application.
        private val SPP_UUID: UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // Serial Port Profile UUID
    }

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _events = MutableSharedFlow<UiEvents>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val events = _events

    suspend fun ensureConnected(): Boolean = withContext(Dispatchers.IO) {
        if (_isConnected.value && bluetoothSocket?.isConnected == true) {
            return@withContext true
        }

        if (bluetoothAdapter == null) {
            showSnackBar("This device doesn't support Bluetooth", color = Color.Red)
            return@withContext false
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val hasPermission =
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                showSnackBar("Bluetooth permission is not granted")
                return@withContext false
            }
        }

        if (!bluetoothAdapter.isEnabled) {
            showSnackBar("Bluetooth is not enabled, please enable it", color = Color.Red)
            return@withContext false
        }

        val device: BluetoothDevice? = try {
            _isLoading.value = true
            bluetoothAdapter
                .bondedDevices
                .firstOrNull { it.name == ROBOT_NAME }
        } catch (se: SecurityException) {
            _isLoading.value = false
            se.printStackTrace()
            showSnackBar("Bluetooth permission is not granted", color = Color.Red)
            _isConnected.value = false
            return@withContext false
        }

        if (device == null) {
            showSnackBar(
                "Device $ROBOT_NAME is not in bonded devices",
                color = Color.Red
            )
            _isConnected.value = false
            _isLoading.value = false
            return@withContext false
        }

        try {
            bluetoothSocket?.close()

            val socket = device.createRfcommSocketToServiceRecord(SPP_UUID)

            try {
                bluetoothAdapter.cancelDiscovery()
            } catch (se: SecurityException) {

                se.printStackTrace()
            }

            socket.connect()

            bluetoothSocket = socket
            outputStream = socket.outputStream
            _isConnected.value = true
            showSnackBar("Connected to $ROBOT_NAME")
            true
        } catch (e: IOException) {
            Log.e("TAG", "ensureConnected: this is the error ${e.message}")
            e.printStackTrace()
            _isConnected.value = false
            showSnackBar("Failed to connect to device", color = Color.Red)
            false
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Low-level: send a single char command (with newline for easier debugging on PC).
     */
    suspend fun sendRawCommandChar(cmd: Char) = withContext(Dispatchers.IO) {
        if (!_isConnected.value || outputStream == null) {
            showSnackBar("The device is not connected", color = Color.Red)
            return@withContext
        }

        try {
            val data = "$cmd".toByteArray()
            outputStream?.write(data)
            outputStream?.flush()
        } catch (e: IOException) {
            e.printStackTrace()
            _isConnected.value = false
            showSnackBar("Failed to send command")
        }
    }

    /**
     * send direction based on RobotDirection enum.
     */
    suspend fun sendActions(direction: RobotActions) {
        val cmd = when (direction) {
            RobotActions.Forward -> 'F'
            RobotActions.Backward -> 'B'
            RobotActions.Left -> 'L'
            RobotActions.Right -> 'R'
            RobotActions.ForwardRight -> 'G'
            RobotActions.ForwardLeft -> 'I'
            RobotActions.BackwardRight -> 'H'
            RobotActions.BackwardLeft -> 'J'
            RobotActions.Stop -> 'S'
            RobotActions.OpenBox -> 'O'
            RobotActions.CloseBox -> 'C'
            else -> return
        }
        sendRawCommandChar(cmd)
    }

    suspend fun setSpeed(speed: RobotSpeed) = withContext(Dispatchers.IO) {
        if (!_isConnected.value || outputStream == null) {
            showSnackBar("The device is not connected", color = Color.Red)
            return@withContext
        }
        try {
            sendRawCommandChar(speed.commandChar)
        } catch (e: Exception) {
            showSnackBar(e.message ?: "Something went wrong", color = Color.Red)
        }
    }

    suspend fun disconnect() = withContext(Dispatchers.IO) {
        try {
            bluetoothSocket?.close()
            showSnackBar("Disconnected")
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            bluetoothSocket = null
            outputStream = null
            _isConnected.value = false
        }
    }

    private suspend fun showSnackBar(message: String, color: Color? = Color.White) {
        withContext(Dispatchers.Main) {
            _events.emit(
                UiEvents.ShowSnackbar(message = message, color = color)
            )
        }
    }
}
