package com.visionstudio.app.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun VisionStudioTheme(content: @Composable () -> Unit) {
    val colors=if(ThemePalette.mode==2) lightColorScheme(
        primary=Cyan,onPrimary=Color.White,secondary=Violet,onSecondary=Color.White,
        background=Night,onBackground=TextPrimary,surface=Panel,onSurface=TextPrimary,
        surfaceVariant=PanelSoft,onSurfaceVariant=TextSecondary,outline=AppStroke
    ) else darkColorScheme(
        primary=Cyan,onPrimary=Night,secondary=Violet,onSecondary=Color.White,
        background=Night,onBackground=TextPrimary,surface=Panel,onSurface=TextPrimary,
        surfaceVariant=PanelSoft,onSurfaceVariant=TextSecondary,outline=AppStroke
    )
    MaterialTheme(colorScheme=colors,typography=Typography(),content=content)
}
