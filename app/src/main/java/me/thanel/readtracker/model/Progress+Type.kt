package me.thanel.readtracker.model

import me.thanel.readtracker.SelectWithBookInformation
import kotlin.math.roundToInt

val SelectWithBookInformation.actualPage: Int
    get() = page ?: percentToPage(percent ?: 0, numPages)

private fun percentToPage(percent: Int, numPages: Int): Int {
    val floatPercent = percent / 100f
    return (floatPercent * numPages).roundToInt()
}
