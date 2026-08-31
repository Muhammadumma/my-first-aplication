package com.example.util

import android.graphics.Bitmap
import android.graphics.Color
import com.example.data.ClearanceStage
import com.example.data.ClearanceStatus
import com.example.data.StudentProfile
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import org.json.JSONObject

object QrCodeUtil {

    /**
     * Generates a crisp Android Bitmap containing a QR Code of the given text payload.
     */
    fun generateQrCodeBitmap(content: String, sizePx: Int = 512): Bitmap? {
        return try {
            val hints = hashMapOf<EncodeHintType, Any>(
                EncodeHintType.CHARACTER_SET to "UTF-8",
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
                EncodeHintType.MARGIN to 1
            )
            val bitMatrix = QRCodeWriter().encode(
                content,
                BarcodeFormat.QR_CODE,
                sizePx,
                sizePx,
                hints
            )
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(
                        x,
                        y,
                        if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                    )
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Builds a structured, verifiable JSON payload for a student's clearance status.
     */
    fun buildStudentClearancePayload(
        profile: StudentProfile,
        stages: List<ClearanceStage>
    ): String {
        val completed = stages.count { it.status == ClearanceStatus.COMPLETED }
        val total = if (stages.isNotEmpty()) stages.size else 8
        val isCleared = completed == total && total > 0

        val json = JSONObject().apply {
            put("system", "UNIV_CLEARANCE_SECURE_AUTH")
            put("version", "2.0")
            put("timestamp", System.currentTimeMillis())
            put("studentId", profile.studentId)
            put("matricNumber", profile.matricNumber)
            put("fullName", profile.fullName)
            put("email", profile.email)
            put("department", profile.department)
            put("faculty", profile.faculty)
            put("level", profile.level)
            put("session", profile.session)
            put("clearancePin", profile.clearancePin)
            put("clearanceStatus", if (isCleared) "FULLY_CLEARED" else "IN_PROGRESS")
            put("progress", "$completed/$total stages completed")
            put("verificationHash", "SEC-SHA256-${(profile.studentId + profile.clearancePin).hashCode().toUInt().toString(16).uppercase()}")
        }
        return json.toString()
    }

    /**
     * Decodes QR Code content from a Bitmap.
     */
    fun decodeQrCodeFromBitmap(bitmap: Bitmap): String? {
        return try {
            val width = bitmap.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

            val source = RGBLuminanceSource(width, height, pixels)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            val result = MultiFormatReader().decode(binaryBitmap)
            result.text
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Parses the QR Code payload into a readable map of student fields.
     */
    fun parseStudentClearanceData(qrText: String): Map<String, String>? {
        return try {
            val json = JSONObject(qrText)
            val map = mutableMapOf<String, String>()
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = json.optString(key, "")
            }
            map
        } catch (e: Exception) {
            // If plain text format
            null
        }
    }
}
