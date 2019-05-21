package me.thanel.readtracker.model

import me.thanel.readtracker.SelectWithBookInformation

val SelectWithBookInformation.progressType: ProgressType
    get() = if (page != null) ProgressType.Page else ProgressType.Percent
