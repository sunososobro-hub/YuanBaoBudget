package com.sosobro.sosomonenote.ui.custom

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.sosobro.sosomonenote.R

class WaterTankView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var fillLevel = 0f
    private var targetLevel = 0f

    private val colorWaterLow = ContextCompat.getColor(context, R.color.yuanbao_accent_blue)
    private val colorWaterMid = ContextCompat.getColor(context, R.color.yuanbao_primary)
    private val colorWaterHigh = ContextCompat.getColor(context, R.color.yuanbao_primary_dark)

    private val colorBorder = ContextCompat.getColor(context, R.color.yuanbao_primary_dark)
    private val colorBg = ContextCompat.getColor(context, R.color.yuanbao_bg)
    private val colorText = ContextCompat.getColor(context, R.color.yuanbao_text_dark)

    private val paintWater = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val paintBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorBorder
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }
    private val paintBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorBg
        style = Paint.Style.FILL
    }
    private val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorText
        textSize = 42f
        textAlign = Paint.Align.CENTER
    }

    fun setLevel(percent: Float) {
        targetLevel = percent.coerceIn(0f, 1f)
        ValueAnimator.ofFloat(fillLevel, targetLevel).apply {
            duration = 1200
            addUpdateListener {
                fillLevel = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()

        canvas.drawRoundRect(5f, 5f, w - 5f, h - 5f, 25f, 25f, paintBg)

        paintWater.color = when {
            fillLevel < 0.5f -> colorWaterLow
            fillLevel < 0.8f -> colorWaterMid
            else -> colorWaterHigh
        }

        val top = h * (1 - fillLevel)
        canvas.drawRoundRect(5f, top, w - 5f, h - 5f, 25f, 25f, paintWater)

        canvas.drawRoundRect(5f, 5f, w - 5f, h - 5f, 25f, 25f, paintBorder)

        val text = "${(fillLevel * 100).toInt()}%"
        canvas.drawText(text, w / 2, h / 2, paintText)
    }
}
