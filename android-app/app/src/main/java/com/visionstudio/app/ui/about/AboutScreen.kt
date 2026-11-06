package com.visionstudio.app.ui.about

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.visionstudio.app.ui.components.VisionMark
import com.visionstudio.app.ui.theme.*

@Composable
fun AboutScreen(onBack:()->Unit) {
    Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Night,DeepNavy))).verticalScroll(rememberScrollState()).padding(20.dp)) {
        TextButton(onClick=onBack,contentPadding=PaddingValues(0.dp)){Text("رجوع ←",color=Cyan)}
        Row(verticalAlignment=Alignment.CenterVertically){
            VisionMark(Modifier.size(58.dp));Spacer(Modifier.width(13.dp))
            Column{Text("عن VisionStudio",fontSize=27.sp,fontWeight=FontWeight.ExtraBold,color=TextPrimary);Text("Image Processing & Computer Vision",fontSize=13.sp,color=Cyan)}
        }
        Spacer(Modifier.height(22.dp))
        InfoCard("إعداد الطلاب","عبدالوهاب عامر  •  Abdulwahab Amer\nعاصم محمد  •  Asim Mohammed\nوائل الحاج  •  Wael Al-Haj")
        Spacer(Modifier.height(12.dp))
        InfoCard("الدكتورة المشرفة","د. سمية الشيبة  •  Dr. Somaya Al-Shaiba")
        Spacer(Modifier.height(12.dp))
        InfoCard("التقنيات","Kotlin • Jetpack Compose • OpenCV\nCameraX • YOLO • TFLite")
        Spacer(Modifier.height(12.dp))
        Card(shape=RoundedCornerShape(22.dp),colors=CardDefaults.cardColors(containerColor=Cyan.copy(alpha=.12f))) {
            Column(Modifier.padding(18.dp)){Text("خصوصية كاملة",fontWeight=FontWeight.Bold,color=Cyan,fontSize=17.sp);Spacer(Modifier.height(5.dp));Text("تعمل الصور والفيديو والنماذج داخل الهاتف، ولا يحتاج التطبيق إلى الإنترنت أثناء التشغيل.",lineHeight=21.sp,color=TextSecondary)}
        }
        Spacer(Modifier.height(24.dp));Text("VisionStudio v1.2",color=TextSecondary,fontSize=12.sp)
    }
}

@Composable private fun InfoCard(title:String,body:String){
    Card(shape=RoundedCornerShape(22.dp),colors=CardDefaults.cardColors(containerColor=Panel),border=androidx.compose.foundation.BorderStroke(1.dp,AppStroke)){
        Column(Modifier.fillMaxWidth().padding(18.dp)){Text(title,fontSize=18.sp,fontWeight=FontWeight.Bold,color=TextPrimary);Spacer(Modifier.height(8.dp));Text(body,fontSize=14.sp,lineHeight=23.sp,color=TextSecondary)}
    }
}
