package com.visionstudio.app.ui.appearance

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.visionstudio.app.ui.theme.*

data class ThemeChoice(val mode:Int,val title:String,val subtitle:String)

@Composable
fun AppearanceScreen(onBack:()->Unit){
    val context=LocalContext.current
    val prefs=context.getSharedPreferences("visionstudio_settings",Context.MODE_PRIVATE)
    var selected by remember{mutableStateOf(prefs.getInt("theme_mode",0))}
    val choices=listOf(
        ThemeChoice(0,"الأزرق الداكن","المظهر الافتراضي لمختبر الرؤية"),
        ThemeChoice(1,"AMOLED أسود","أسود عميق مع لون أخضر تقني"),
        ThemeChoice(2,"المظهر الفاتح","خلفية فاتحة ونص داكن")
    )
    Column(Modifier.fillMaxSize().background(Night).padding(18.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){
            Text("المظهر والألوان",color=TextPrimary,fontSize=28.sp,fontWeight=FontWeight.Bold)
            Text("رجوع ←",color=Cyan,fontSize=18.sp,modifier=Modifier.clickable{onBack()}.padding(8.dp))
        }
        Text("اختر النمط المناسب. يُحفظ الاختيار على الهاتف ويعمل دون إنترنت.",color=TextSecondary,fontSize=15.sp)
        choices.forEach{choice->
            Card(colors=CardDefaults.cardColors(containerColor=Panel),shape=RoundedCornerShape(20.dp),modifier=Modifier.fillMaxWidth().clickable{
                selected=choice.mode;prefs.edit().putInt("theme_mode",choice.mode).apply();ThemePalette.mode=choice.mode;(context as? Activity)?.recreate()
            }){
                Row(Modifier.fillMaxWidth().padding(18.dp),verticalAlignment=Alignment.CenterVertically){
                    RadioButton(selected=selected==choice.mode,onClick=null)
                    Spacer(Modifier.width(10.dp))
                    Column{Text(choice.title,color=TextPrimary,fontSize=20.sp,fontWeight=FontWeight.Bold);Text(choice.subtitle,color=TextSecondary,fontSize=14.sp)}
                }
            }
        }
        Text("قد تبقى شاشة الكاميرا والفيديو بالتصميم الداكن لضمان وضوح المعاينة.",color=TextSecondary,fontSize=13.sp)
    }
}
