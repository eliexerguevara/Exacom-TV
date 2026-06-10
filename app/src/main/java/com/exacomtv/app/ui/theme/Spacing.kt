package com.exacomtv.app.ui.theme

import com.exacomtv.app.ui.design.AppSpacing
import com.exacomtv.app.ui.design.LocalAppSpacing

typealias Spacing = AppSpacing

val LocalSpacing = LocalAppSpacing

fun defaultSpacing(): Spacing = AppSpacing()
