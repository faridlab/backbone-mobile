package id.startapp.presentation.util

/**
 * Platform-specific sharing helper.
 *
 * On Android: Uses Intent for WhatsApp and general sharing.
 * On iOS: Not yet implemented (placeholder).
 */
expect object ShareHelper {
    /**
     * Share text via WhatsApp to a specific phone number.
     * @param phone Phone number in international format (e.g., "6281234567890")
     * @param message Message text to send
     */
    fun shareViaWhatsApp(phone: String, message: String)

    /**
     * Share text using the system share sheet.
     * @param text Text to share
     * @param title Optional title for the share dialog
     */
    fun shareText(text: String, title: String = "")
}
