package com.visionstudio.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.visionstudio.app.navigation.AppNavigation
import com.visionstudio.app.ui.theme.ThemePalette
import com.visionstudio.app.ui.theme.VisionStudioTheme

class MainActivity:ComponentActivity(){
    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        ThemePalette.mode=getSharedPreferences("visionstudio_settings",MODE_PRIVATE).getInt("theme_mode",0)
        setContent{VisionStudioTheme{CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl){AppNavigation()}}}
    }
}
