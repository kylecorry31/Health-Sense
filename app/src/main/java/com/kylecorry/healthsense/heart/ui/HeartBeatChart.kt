package com.kylecorry.healthsense.heart.ui

import android.graphics.Color
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.kylecorry.trailsensecore.infrastructure.system.UiUtils
import kotlin.math.max
import kotlin.math.min


class HeartBeatChart(private val chart: LineChart, private val color: Int) {

    private var granularity = 0.5f

    init {
        chart.description.isEnabled = false
        chart.setTouchEnabled(false)
        chart.isDragEnabled = false
        chart.setScaleEnabled(false)
        chart.setDrawGridBackground(false)
        chart.setDrawBorders(false)

        chart.xAxis.setDrawLabels(false)
        chart.axisRight.setDrawLabels(false)

        val primaryColor = UiUtils.androidTextColorPrimary(chart.context)
        val r = primaryColor.red
        val g = primaryColor.green
        val b = primaryColor.blue

        chart.xAxis.setDrawGridLines(false)
        chart.axisLeft.setDrawGridLines(false)
        chart.axisLeft.setDrawLabels(false)
        chart.axisLeft.gridColor = Color.argb(50, r, g, b)
        chart.axisLeft.textColor = Color.argb(150, r, g, b)
        chart.axisRight.setDrawGridLines(false)
        chart.xAxis.setDrawAxisLine(false)
        chart.axisLeft.setDrawAxisLine(false)
        chart.axisRight.setDrawAxisLine(false)
        chart.setNoDataText("")
    }

    fun plot(data: List<Float>, peaks: List<Int> = listOf()) {
        val values = data.mapIndexed { index, value -> Entry(index.toFloat(), value) }
        val set1 = LineDataSet(values, "Heart Beat")
        set1.color = color
        set1.fillAlpha = 180
        set1.lineWidth = 3f
        set1.setDrawValues(false)
        set1.fillColor = color
        set1.setCircleColor(color)
        set1.setDrawCircleHole(false)
        set1.setDrawCircles(true)
        set1.circleRadius = 1.5f
        set1.setDrawFilled(false)

        val peakValues = peaks.map { value -> Entry(value.toFloat(), data[value]) }
        val set2 = LineDataSet(peakValues, "Peaks")
        set2.color = Color.TRANSPARENT
        set2.fillAlpha = 0
        set2.lineWidth = 0f
        set2.setDrawValues(false)
        set2.fillColor = Color.TRANSPARENT
        set2.setCircleColor(Color.WHITE)
        set2.setDrawCircleHole(false)
        set2.setDrawCircles(true)
        set2.circleRadius = 2f
        set2.setDrawFilled(false)


        val lineData = LineData(set1, set2)
        chart.data = lineData
        chart.legend.isEnabled = false
        chart.notifyDataSetChanged()
        chart.invalidate()
    }
}