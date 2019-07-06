package me.thanel.readtracker.util

import kotlin.math.roundToInt

// TODO: Write tests
fun Int.toIntPercentOf(maxValue: Int): Int {
    val floatPercent = this / maxValue.toFloat()
    return (floatPercent * 100).roundToInt()
}

// TODO: Write tests
fun Int.fromIntPercentOf(maxValue: Int): Int {
    val floatPercent = this / 100f
    return (floatPercent * maxValue).roundToInt()
}
