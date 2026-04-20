package id.startapp.pheromone.presentation.util.printing

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import id.startapp.pheromone.presentation.util.PlatformContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID

/**
 * Android implementation of [BluetoothPrinterManager].
 *
 * Uses Android BluetoothSocket with SPP (Serial Port Profile) UUID
 * to connect to thermal printers and send ESC/POS data.
 */
actual class BluetoothPrinterManager {

    companion object {
        /** Standard SPP UUID for Bluetooth serial communication */
        private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    @SuppressLint("MissingPermission")
    actual fun getPairedPrinters(): List<PrinterDevice> {
        val context = PlatformContext.applicationContext ?: return emptyList()
        if (!hasBluetoothConnectPermission(context)) return emptyList()
        val bluetoothManager = context.getSystemService(BluetoothManager::class.java) ?: return emptyList()
        val adapter = bluetoothManager.adapter ?: return emptyList()

        return try {
            adapter.bondedDevices
                .orEmpty()
                .map { device ->
                    PrinterDevice(
                        name = device.name ?: "Unknown",
                        address = device.address,
                        isPaired = true
                    )
                }
                .sortedBy { it.name }
        } catch (e: SecurityException) {
            emptyList()
        }
    }

    @SuppressLint("MissingPermission")
    actual suspend fun print(address: String, data: ByteArray): PrintResult = try {
        kotlinx.coroutines.withTimeout(15_000L) { withContext(Dispatchers.IO) {
            val context = PlatformContext.applicationContext
                ?: return@withContext PrintResult.Error("Application context not available")

            if (!hasBluetoothConnectPermission(context)) {
                return@withContext PrintResult.Error("Bluetooth permission not granted. Please enable BLUETOOTH_CONNECT in app settings.")
            }

            val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
                ?: return@withContext PrintResult.Error("Bluetooth not available")

            val adapter = bluetoothManager.adapter
                ?: return@withContext PrintResult.Error("Bluetooth adapter not found")

            if (!adapter.isEnabled) {
                return@withContext PrintResult.Error("Bluetooth is disabled")
            }

            val device: BluetoothDevice = try {
                adapter.getRemoteDevice(address)
            } catch (e: IllegalArgumentException) {
                return@withContext PrintResult.Error("Invalid printer address")
            }

            var socket: BluetoothSocket? = null
            try {
                socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
                adapter.cancelDiscovery()
                socket.connect()
                socket.outputStream.write(data)
                socket.outputStream.flush()
                PrintResult.Success
            } catch (e: SecurityException) {
                PrintResult.Error("Bluetooth permission denied")
            } catch (e: IOException) {
                PrintResult.Error("Connection failed: ${e.message ?: "Unknown error"}")
            } finally {
                try {
                    socket?.close()
                } catch (_: IOException) {
                    // Ignore close errors
                }
            }
        } }
    } catch (_: kotlinx.coroutines.TimeoutCancellationException) {
        PrintResult.Error("Connection timed out. Is the printer powered on?")
    }

    private fun hasBluetoothConnectPermission(context: android.content.Context): Boolean {
        // BLUETOOTH_CONNECT permission is only required on Android 12+ (API 31+)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        return ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) ==
            PackageManager.PERMISSION_GRANTED
    }
}

actual fun createBluetoothPrinterManager(): BluetoothPrinterManager = BluetoothPrinterManager()
