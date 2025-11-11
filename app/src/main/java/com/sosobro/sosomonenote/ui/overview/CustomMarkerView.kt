package com.sosobro.sosomonenote.ui.overview

import android.content.Context
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.sosobro.sosomonenote.R
import java.text.DecimalFormat

class CustomMarkerView(
    context: Context,
    layoutResource: Int,
    private val labels: List<String>
) : MarkerView(context, layoutResource) {

    private val tvContent: TextView = findViewById(R.id.tvMarkerContent)
    private val formatter = DecimalFormat("#,###.##")

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        e?.let {
            val index = e.x.toInt().coerceIn(labels.indices)
            val date = labels[index]
            val value = formatter.format(e.y)
            tvContent.text = "📅 $date\n💰 NT$ $value"
        }
        super.refreshContent(e, highlight)
    }

    // ✅ 修正版：避免 chartView 為 null 時 crash
    override fun getOffsetForDrawingAtPoint(xpos: Float, ypos: Float): MPPointF {
        val offset = MPPointF(-(width / 2f), -height.toFloat())
        val chart = chartView ?: return offset // <-- 防止 NullPointerException

        // ✅ 控制 marker 不超出邊界
        if (xpos + offset.x < 0) offset.x = 0f
        if (xpos + width / 2f > chart.width) offset.x = -width.toFloat()
        if (ypos + offset.y < 0) offset.y = 0f
        return offset
    }
}
