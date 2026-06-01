package id.startapp.presentation.util.printing

import id.startapp.presentation.util.NumberFormatUtils
import id.startapp.presentation.util.ReceiptData
import id.startapp.presentation.util.ReceiptItem

/**
 * ESC/POS command builder for thermal receipt printers.
 *
 * Generates byte arrays containing ESC/POS commands for
 * 58mm (32 chars/line) and 80mm (48 chars/line) printers.
 *
 * @param charsPerLine Number of characters per line (32 for 58mm, 48 for 80mm)
 */
class EscPosBuilder(private val charsPerLine: Int = 32) {

    private val buffer = mutableListOf<Byte>()

    companion object {
        const val ESC: Byte = 0x1B
        const val GS: Byte = 0x1D
        const val LF: Byte = 0x0A

        // Alignment
        val ALIGN_LEFT = byteArrayOf(ESC, 0x61, 0x00)
        val ALIGN_CENTER = byteArrayOf(ESC, 0x61, 0x01)
        val ALIGN_RIGHT = byteArrayOf(ESC, 0x61, 0x02)

        // Font styling
        val BOLD_ON = byteArrayOf(ESC, 0x45, 0x01)
        val BOLD_OFF = byteArrayOf(ESC, 0x45, 0x00)
        val DOUBLE_SIZE = byteArrayOf(ESC, 0x21, 0x30)
        val NORMAL_SIZE = byteArrayOf(ESC, 0x21, 0x00)

        // Paper control
        val INIT = byteArrayOf(ESC, 0x40)
        val CUT = byteArrayOf(GS, 0x56, 0x42, 0x00)
        val FEED_3 = byteArrayOf(ESC, 0x64, 0x03)
    }

    // ── Builder methods ─────────────────────────────────────────────────

    fun init(): EscPosBuilder {
        buffer.clear()
        buffer.addAll(INIT.toList())
        return this
    }

    fun alignCenter(): EscPosBuilder {
        buffer.addAll(ALIGN_CENTER.toList())
        return this
    }

    fun alignLeft(): EscPosBuilder {
        buffer.addAll(ALIGN_LEFT.toList())
        return this
    }

    fun alignRight(): EscPosBuilder {
        buffer.addAll(ALIGN_RIGHT.toList())
        return this
    }

    fun boldOn(): EscPosBuilder {
        buffer.addAll(BOLD_ON.toList())
        return this
    }

    fun boldOff(): EscPosBuilder {
        buffer.addAll(BOLD_OFF.toList())
        return this
    }

    fun doubleSize(): EscPosBuilder {
        buffer.addAll(DOUBLE_SIZE.toList())
        return this
    }

    fun normalSize(): EscPosBuilder {
        buffer.addAll(NORMAL_SIZE.toList())
        return this
    }

    fun feed(lines: Int = 1): EscPosBuilder {
        repeat(lines) { buffer.add(LF) }
        return this
    }

    fun cut(): EscPosBuilder {
        buffer.addAll(FEED_3.toList())
        buffer.addAll(CUT.toList())
        return this
    }

    fun text(s: String): EscPosBuilder {
        buffer.addAll(s.encodeToByteArray().toList())
        buffer.add(LF)
        return this
    }

    fun separator(char: Char = '-'): EscPosBuilder {
        return text(char.toString().repeat(charsPerLine))
    }

    fun doubleSeparator(): EscPosBuilder = separator('=')

    /**
     * Print two columns: left-aligned label and right-aligned value.
     */
    fun twoColumn(left: String, right: String): EscPosBuilder {
        val gap = charsPerLine - left.length - right.length
        return if (gap > 0) {
            text(left + " ".repeat(gap) + right)
        } else {
            text(left)
            alignRight()
            text(right)
            alignLeft()
            this
        }
    }

    fun build(): ByteArray = buffer.toByteArray()

    // ── Receipt builder ─────────────────────────────────────────────────

    /**
     * Build a complete receipt from order data.
     *
     * Produces a byte array with ESC/POS commands ready to send to a
     * thermal printer. Uses bold headers, centered shop name, and
     * two-column totals layout.
     */
    fun buildReceipt(
        orderNumber: String,
        outletName: String,
        outletAddress: String?,
        customerName: String,
        orderDate: String,
        items: List<ReceiptItem>,
        subtotal: Double,
        deliveryFee: Double,
        platformFee: Double,
        taxAmount: Double,
        discountAmount: Double,
        grandTotal: Double,
        paidAmount: Double,
        paymentMethod: String,
        estimatedReady: String?,
    ): ByteArray {
        init()
        buildHeader(outletName, outletAddress)
        buildOrderInfo(orderNumber, orderDate, customerName)
        buildItems(items)
        buildTotals(subtotal, deliveryFee, platformFee, taxAmount, discountAmount)
        buildPayment(grandTotal, paidAmount, paymentMethod, estimatedReady)
        buildFooter()
        return build()
    }

    private fun buildHeader(outletName: String, outletAddress: String?) {
        alignCenter()
        boldOn()
        doubleSize()
        text("Backbone")
        normalSize()
        boldOff()
        text(outletName)
        if (!outletAddress.isNullOrBlank()) {
            text(outletAddress)
        }
        doubleSeparator()
    }

    private fun buildOrderInfo(orderNumber: String, orderDate: String, customerName: String) {
        alignLeft()
        text("No   : $orderNumber")
        text("Tgl  : $orderDate")
        text("Nama : $customerName")
        feed()
    }

    private fun buildItems(items: List<ReceiptItem>) {
        boldOn()
        text("DAFTAR PESANAN:")
        boldOff()
        separator()
        items.forEachIndexed { index, item ->
            val num = index + 1
            if (!item.serviceName.isNullOrBlank()) {
                text("$num. ${item.serviceName}")
                twoColumn("   ${item.name} x${item.quantity}", "Rp ${formatNumber(item.total)}")
            } else {
                twoColumn("$num. ${item.name} x${item.quantity}", "Rp ${formatNumber(item.total)}")
            }
        }
        separator()
    }

    private fun buildTotals(
        subtotal: Double,
        deliveryFee: Double,
        platformFee: Double,
        taxAmount: Double,
        discountAmount: Double,
    ) {
        twoColumn("Subtotal", "Rp ${formatNumber(subtotal)}")
        twoColumn("Biaya Layanan", "Rp ${formatNumber(platformFee)}")
        twoColumn("Ongkir", "Rp ${formatNumber(deliveryFee)}")
        twoColumn("Pajak", "Rp ${formatNumber(taxAmount)}")
        if (discountAmount > 0.0) {
            twoColumn("Diskon", "-Rp ${formatNumber(discountAmount)}")
        } else {
            twoColumn("Diskon", "-Rp 0")
        }
        doubleSeparator()
    }

    private fun buildPayment(
        grandTotal: Double,
        paidAmount: Double,
        paymentMethod: String,
        estimatedReady: String?,
    ) {
        boldOn()
        twoColumn("TOTAL", "Rp ${formatNumber(grandTotal)}")
        boldOff()
        twoColumn("Bayar", "Rp ${formatNumber(paidAmount)}")
        val change = paidAmount - grandTotal
        twoColumn("Kembalian", "Rp ${formatNumber(if (change > 0) change else 0.0)}")
        doubleSeparator()
        text("Metode: $paymentMethod")
        if (!estimatedReady.isNullOrBlank()) {
            text("Est. Selesai: $estimatedReady")
        }
        feed()
    }

    private fun buildFooter() {
        alignCenter()
        boldOn()
        text("Terima kasih!")
        boldOff()
        text("Backbone")
        cut()
    }

    /**
     * Build a receipt from a [ReceiptData] model.
     */
    fun buildReceipt(data: ReceiptData): ByteArray = buildReceipt(
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
}
