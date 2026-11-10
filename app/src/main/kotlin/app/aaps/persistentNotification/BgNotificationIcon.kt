package app.aaps.persistentNotification

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Typeface
import androidx.core.graphics.drawable.IconCompat

/**
 * Draws the glucose value as a status bar icon.
 * A struck-through value marks stale data.
 */
internal object BgNotificationIcon {

    private const val SIZE_PX = 96

    fun create(text: String, strikeThrough: Boolean = false): IconCompat {
        val bitmap = Bitmap.createBitmap(SIZE_PX, SIZE_PX, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            // Fit the width first, then cap the height so short values like "98" do not overflow.
            textSize = SIZE_PX.toFloat()
            val width = measureText(text)
            if (width > SIZE_PX) textSize = textSize * SIZE_PX / width
            textSize = textSize.coerceAtMost(SIZE_PX * 0.8f)
        }
        val baseline = SIZE_PX / 2f - (paint.descent() + paint.ascent()) / 2f
        canvas.drawText(text, SIZE_PX / 2f, baseline, paint)
        if (strikeThrough) {
            val cut = Paint().apply {
                xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
                strokeWidth = SIZE_PX * 0.09f
            }
            // Digits are about 0.7 em tall, so their middle sits 0.35 em above the baseline.
            val y = baseline - paint.textSize * 0.35f
            canvas.drawLine(0f, y, SIZE_PX.toFloat(), y, cut)
        }
        return IconCompat.createWithBitmap(bitmap)
    }
}
