package app.aaps.persistentNotification

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.Typeface
import androidx.core.graphics.drawable.IconCompat

/**
 * Draws the glucose value as a status bar icon: a filled badge with the value cut out of it.
 * Stale data is a dashed, empty badge with a dimmed value.
 */
internal object BgNotificationIcon {

    private const val SIZE_PX = 96f
    private const val BADGE_HEIGHT_PX = 62f
    private const val CORNER_PX = 16f
    private const val STALE_STROKE_PX = 5f
    private const val STALE_TEXT_ALPHA = 150

    fun create(text: String, stale: Boolean = false): IconCompat {
        val bitmap = Bitmap.createBitmap(SIZE_PX.toInt(), SIZE_PX.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val badge = RectF(0f, (SIZE_PX - BADGE_HEIGHT_PX) / 2f, SIZE_PX, (SIZE_PX + BADGE_HEIGHT_PX) / 2f)
        if (stale) {
            // Keep the whole stroke inside the bitmap.
            badge.inset(STALE_STROKE_PX / 2f, STALE_STROKE_PX / 2f)
            val outline = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                style = Paint.Style.STROKE
                strokeWidth = STALE_STROKE_PX
                pathEffect = DashPathEffect(floatArrayOf(9f, 7f), 0f)
            }
            canvas.drawRoundRect(badge, CORNER_PX - STALE_STROKE_PX / 2f, CORNER_PX - STALE_STROKE_PX / 2f, outline)
            val paint = textPaint(text, maxWidth = SIZE_PX - 22f, maxSize = BADGE_HEIGHT_PX * 0.78f).apply { alpha = STALE_TEXT_ALPHA }
            canvas.drawText(text, SIZE_PX / 2f, baseline(paint), paint)
        } else {
            canvas.drawRoundRect(badge, CORNER_PX, CORNER_PX, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE })
            // The status bar tints the icon by its alpha only, so the value is cut out instead of drawn in a second colour.
            val paint = textPaint(text, maxWidth = SIZE_PX - 14f, maxSize = BADGE_HEIGHT_PX * 0.86f).apply {
                xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            }
            canvas.drawText(text, SIZE_PX / 2f, baseline(paint), paint)
        }
        return IconCompat.createWithBitmap(bitmap)
    }

    // Fit the width first, then cap the height so short values like "98" do not overflow the badge.
    private fun textPaint(text: String, maxWidth: Float, maxSize: Float) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        // Condensed digits leave more height for three digit and mmol/l values.
        typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        textSize = SIZE_PX
        val width = measureText(text)
        if (width > maxWidth) textSize = textSize * maxWidth / width
        textSize = textSize.coerceAtMost(maxSize)
    }

    private fun baseline(paint: Paint) = SIZE_PX / 2f - (paint.descent() + paint.ascent()) / 2f
}
