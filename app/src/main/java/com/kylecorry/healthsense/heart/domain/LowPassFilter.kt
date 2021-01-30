package com.kylecorry.healthsense.heart.domain

class MovingAverageFilter(var size: Int) {

    private val window = mutableListOf<Float>()

    fun filter(measurement: Float): Float {
        window.add(measurement)
        if (window.size > size){
            window.removeAt(0)
        }
        return window.average().toFloat()
    }
}