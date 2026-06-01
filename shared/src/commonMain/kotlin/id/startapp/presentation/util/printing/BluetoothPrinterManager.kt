package id.startapp.presentation.util.printing

/**
 * Platform-specific Bluetooth printer manager.
 *
 * Discovers paired Bluetooth printers, connects, and sends
 * raw byte data (ESC/POS commands) for receipt printing.
 *
 * Android: Uses BluetoothSocket with SPP UUID.
 * iOS: Not yet implemented.
 */

data class PrinterDevice(
    val name: String,
    val address: String,
    val isPaired: Boolean = true
)

sealed class PrintResult {
    data object Success : PrintResult()
    data class Error(val message: String) : PrintResult()
}

expect class BluetoothPrinterManager {
    /**
     * Get list of paired Bluetooth devices that are likely printers.
     * Filters by common printer Bluetooth classes or all paired devices.
     */
    fun getPairedPrinters(): List<PrinterDevice>

    /**
     * Send raw bytes (ESC/POS data) to a Bluetooth printer.
     * @param address Bluetooth MAC address of the printer
     * @param data ESC/POS command bytes to send
     * @return PrintResult indicating success or error
     */
    suspend fun print(address: String, data: ByteArray): PrintResult
}

/**
 * Factory function to create [BluetoothPrinterManager] instances.
 * Works around expect/actual constructor limitations in KMP.
 */
expect fun createBluetoothPrinterManager(): BluetoothPrinterManager
