package com.ghostid.app.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.ghostid.app.domain.model.Alias
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

object QrCodeGenerator {

    fun generateForAlias(alias: Alias, size: Int = 512): Bitmap {
        val vcard = buildVCard(alias)
        return encode(vcard, size)
    }

    private fun buildVCard(alias: Alias): String = buildString {
        appendLine("BEGIN:VCARD")
        appendLine("VERSION:3.0")
        appendLine("FN:${alias.name.full}")
        appendLine("N:${alias.name.lastName};${alias.name.firstName};;;")
        appendLine("BDAY:${alias.dateOfBirth.replace("-", "")}")
        appendLine("TEL;TYPE=CELL:${alias.phoneNumber}")
        appendLine("ADR;TYPE=HOME:;;${alias.address.street};${alias.address.city};;${alias.address.postcode};${alias.address.country}")
        appendLine("TITLE:${alias.occupation}")
        val emailAccount = alias.accounts.firstOrNull { it.platform.name.startsWith("EMAIL") }
        if (emailAccount != null) appendLine("EMAIL:${emailAccount.username}")
        appendLine("NOTE:${alias.bio.take(200)}")
        appendLine("END:VCARD")
    }

    private fun encode(content: String, size: Int): Bitmap {
        val hints = mapOf(EncodeHintType.MARGIN to 1)
        val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }
}
