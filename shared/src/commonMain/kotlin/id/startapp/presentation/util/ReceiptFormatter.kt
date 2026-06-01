package id.startapp.presentation.util

/**
 * Formats order data into a plain-text receipt string for sharing and printing.
 *
 * Uses Indonesian locale formatting (dots as thousands separator, "Rp" prefix).
 */
object ReceiptFormatter {

    /**
     * Format a complete receipt as plain text.
     *
     * @param orderNumber Order number (e.g., "#260309VVJ5")
     * @param outletName Provider outlet name
     * @param outletAddress Provider outlet address (optional)
     * @param customerName Customer name
     * @param orderDate Formatted date string
     * @param items List of ReceiptItem
     * @param subtotal Subtotal amount
     * @param deliveryFee Delivery fee (0 if none)
     * @param platformFee Platform/service fee
     * @param taxAmount Tax amount
     * @param discountAmount Discount amount (0 if none)
     * @param grandTotal Grand total
     * @param paidAmount Amount paid
     * @param paymentMethod Payment method string
     * @param estimatedReady Estimated ready date/time string
     * @return Formatted receipt text
     */
    fun formatReceipt(
        orderNumber: String,
        outletName: String,
        outletAddress: String? = null,
        customerName: String,
        orderDate: String,
        items: List<ReceiptItem>,
        subtotal: Double,
        deliveryFee: Double = 0.0,
        platformFee: Double = 0.0,
        taxAmount: Double = 0.0,
        discountAmount: Double = 0.0,
        grandTotal: Double,
        paidAmount: Double,
        paymentMethod: String,
        estimatedReady: String? = null,
    ): String {
        val sb = StringBuilder()
        val sep = "================================"
        val thinSep = "--------------------------------"

        // Header
        sb.appendLine(sep)
        sb.appendLine("    Backbone")
        sb.appendLine("    $outletName")
        if (!outletAddress.isNullOrBlank()) {
            sb.appendLine("    $outletAddress")
        }
        sb.appendLine(sep)

        // Order info
        sb.appendLine("No   : $orderNumber")
        sb.appendLine("Tgl  : $orderDate")
        sb.appendLine("Nama : $customerName")
        sb.appendLine()

        // Items header
        sb.appendLine("DAFTAR PESANAN:")
        sb.appendLine(thinSep)

        items.forEachIndexed { index, item ->
            val num = index + 1
            if (!item.serviceName.isNullOrBlank()) {
                sb.appendLine("$num. ${item.serviceName}")
                sb.appendLine("   ${item.name} x${item.quantity}    Rp ${formatNumber(item.total)}")
            } else {
                sb.appendLine("$num. ${item.name} x${item.quantity}    Rp ${formatNumber(item.total)}")
            }
        }

        sb.appendLine(thinSep)

        // Totals
        sb.appendLine(padLine("Subtotal", "Rp ${formatNumber(subtotal)}"))
        sb.appendLine(padLine("Biaya Layanan", "Rp ${formatNumber(platformFee)}"))
        sb.appendLine(padLine("Ongkir", "Rp ${formatNumber(deliveryFee)}"))
        sb.appendLine(padLine("Pajak", "Rp ${formatNumber(taxAmount)}"))
        if (discountAmount > 0.0) {
            sb.appendLine(padLine("Diskon", "-Rp ${formatNumber(discountAmount)}"))
        } else {
            sb.appendLine(padLine("Diskon", "-Rp 0"))
        }

        sb.appendLine(sep)

        // Grand total & payment
        sb.appendLine(padLine("TOTAL", "Rp ${formatNumber(grandTotal)}"))
        sb.appendLine(padLine("Bayar", "Rp ${formatNumber(paidAmount)}"))

        val change = paidAmount - grandTotal
        sb.appendLine(padLine("Kembalian", "Rp ${formatNumber(if (change > 0) change else 0.0)}"))

        sb.appendLine(sep)

        // Footer
        sb.appendLine("Metode: $paymentMethod")
        if (!estimatedReady.isNullOrBlank()) {
            sb.appendLine("Est. Selesai: $estimatedReady")
        }
        sb.appendLine()
        sb.appendLine("Terima kasih!")
        sb.appendLine("Backbone")

        return sb.toString()
    }

    /**
     * Format a receipt from a [ReceiptData] model.
     */
    fun formatReceipt(data: ReceiptData): String = formatReceipt(
        orderNumber = data.orderNumber,
        outletName = data.outletName,
        outletAddress = data.outletAddress,
        customerName = data.customerName,
        orderDate = data.orderDate,
        items = data.items,
        subtotal = data.subtotal,
        deliveryFee = data.deliveryFee,
        platformFee = data.platformFee,
        taxAmount = data.taxAmount,
        discountAmount = data.discountAmount,
        grandTotal = data.grandTotal,
        paidAmount = data.paidAmount,
        paymentMethod = data.paymentMethod,
        estimatedReady = data.estimatedReady,
    )

    private fun formatNumber(value: Double): String = NumberFormatUtils.formatNumber(value)

    /**
     * Pad a label and value across a 32-character receipt line.
     */
    private fun padLine(label: String, value: String, width: Int = 32): String {
        val gap = width - label.length - value.length
        return if (gap > 0) {
            label + " ".repeat(gap) + value
        } else {
            "$label  $value"
        }
    }
}

/**
 * Represents a single line item on a receipt.
 */
data class ReceiptItem(
    val name: String,
    val serviceName: String?,
    val quantity: Int,
    val unitPrice: Double,
    val total: Double,
)

/**
 * All data needed to render a receipt (text or ESC/POS).
 *
 * Single source of truth consumed by both [ReceiptFormatter] and
 * [id.startapp.presentation.util.printing.EscPosBuilder].
 */
data class ReceiptData(
    val orderNumber: String,
    val outletName: String,
    val outletAddress: String? = null,
    val customerName: String,
    val orderDate: String,
    val items: List<ReceiptItem>,
    val subtotal: Double,
    val deliveryFee: Double = 0.0,
    val platformFee: Double = 0.0,
    val taxAmount: Double = 0.0,
    val discountAmount: Double = 0.0,
    val grandTotal: Double,
    val paidAmount: Double,
    val paymentMethod: String,
    val estimatedReady: String? = null,
)
