package com.sosobro.sosomonenote.ui.custom

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class WaterTankView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var fillLevel = 0f
    private var targetLevel = 0f

    private val paintWater = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#5B9BD5")
        style = Paint.Style.FILL
    }
    private val paintBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#666666")
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }
    private val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        textSize = 42f
        textAlign = Paint.Align.CENTER
    }

    fun setLevel(percent: Float) {
        targetLevel = percent.coerceIn(0f, 1f)
        val animator = ValueAnimator.ofFloat(fillLevel, targetLevel)
        animator.duration = 1200
        animator.addUpdateListener {
            fillLevel = it.animatedValue as Float
            invalidate()
        }
        animator.start()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()

        // 水缸邊框
        canvas.drawRoundRect(5f, 5f, w - 5f, h - 5f, 25f, 25f, paintBorder)

        // 水位顏色依比例變化
        paintWater.color = when {
            fillLevel < 0.5f -> Color.parseColor("#5B9BD5") // 藍
            fillLevel < 0.8f -> Color.parseColor("#FFC107") // 黃
            else -> Color.parseColor("#F44336") // 紅
        }

        // 畫水面
        val top = h * (1 - fillLevel)
        canvas.drawRoundRect(5f, top, w - 5f, h - 5f, 25f, 25f, paintWater)

        // 百分比文字
        val text = "${(fillLevel * 100).toInt()}%"
        canvas.drawText(text, w / 2, h / 2, paintText)
    }
}
