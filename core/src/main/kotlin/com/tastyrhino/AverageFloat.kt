package com.tastyrhino

class AverageFloat(count: Int = 100, startValue: Float = 0.0f) {

    private val vals = MutableList(count) { startValue }
    private var counter = 0

    fun update(currentVal: Float) {
        vals[counter] = currentVal
        counter++
        if (counter >= vals.size) {
            counter = 0
        }
    }

    fun average() = vals.average().toFloat()

    fun init(startValue: Float) {
        vals.indices.forEach { vals[it] = startValue }
    }
}