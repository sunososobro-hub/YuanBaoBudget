package com.sosobro.sosomonenote.ui.analysis

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

    // ✅ 新版使用這個方法取代 getXOffset / getYOffset
    override fun getOffset(): MPPointF {
        return MPPointF(-(width / 2f), -height.toFloat())
    }
}
