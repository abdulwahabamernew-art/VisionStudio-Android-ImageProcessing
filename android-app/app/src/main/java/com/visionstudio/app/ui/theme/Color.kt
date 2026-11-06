package com.visionstudio.app.ui.theme

import androidx.compose.ui.graphics.Color

object ThemePalette { var mode:Int=0 }
private fun pick(navy:Long,amoled:Long,light:Long)=Color(when(ThemePalette.mode){1->amoled;2->light;else->navy})

val Night get()=pick(0xFF07111F,0xFF000000,0xFFF3F7FC)
val DeepNavy get()=pick(0xFF0D1B2E,0xFF070707,0xFFE8EFF7)
val Panel get()=pick(0xFF12243A,0xFF111111,0xFFFFFFFF)
val PanelSoft get()=pick(0xFF172D46,0xFF191919,0xFFE1EAF4)
val Cyan get()=pick(0xFF27D7E8,0xFF31E6A1,0xFF0077CC)
val ElectricBlue get()=pick(0xFF2E8CFF,0xFF58A6FF,0xFF005BBB)
val Violet get()=pick(0xFF7A5CFF,0xFFBB86FC,0xFF6750A4)
val TextPrimary get()=pick(0xFFF4F8FF,0xFFFFFFFF,0xFF142033)
val TextSecondary get()=pick(0xFFA9BCD2,0xFFB8B8B8,0xFF53657A)
val Success get()=pick(0xFF43E0A1,0xFF31E6A1,0xFF16875B)
val AppStroke get()=pick(0xFF28425F,0xFF333333,0xFFBAC8D8)
