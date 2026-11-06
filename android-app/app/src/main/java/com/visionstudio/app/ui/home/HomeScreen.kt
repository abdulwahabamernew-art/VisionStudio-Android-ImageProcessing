package com.visionstudio.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.visionstudio.app.ui.components.*
import com.visionstudio.app.ui.theme.*

data class HomeFeature(val icon:ToolIcon,val title:String,val subtitle:String,val route:String,val ready:Boolean,val accent:androidx.compose.ui.graphics.Color)

@Composable
fun HomeScreen(onNavigate:(String)->Unit,onPending:(String)->Unit) {
    val features=listOf(
        HomeFeature(ToolIcon.Camera,"الكاميرا المباشرة","تصوير وفلاتر لحظية","camera",true,Cyan),
        HomeFeature(ToolIcon.Image,"معالجة صورة","فتح، تحسين وتحرير","image",true,ElectricBlue),
        HomeFeature(ToolIcon.Video,"معالجة فيديو","فلاتر وتصدير الفيديو","video",true,Violet),
        HomeFeature(ToolIcon.Filters,"مختبر الفلاتر","OpenCV • قبل / بعد","processing",true,Cyan),
        HomeFeature(ToolIcon.Detect,"كشف الأجسام","YOLO محلي دون إنترنت","detection",true,ElectricBlue),
        HomeFeature(ToolIcon.Results,"النتائج","حفظ، عرض ومشاركة","results",false,Violet),
        HomeFeature(ToolIcon.Results,"محاضرات المقرر","10 Labs • Python / OpenCV","lectures",true,Violet),
        HomeFeature(ToolIcon.Filters,"المظهر والألوان","داكن • AMOLED • فاتح","appearance",true,Cyan),
        HomeFeature(ToolIcon.About,"عن المشروع","الفريق والتقنيات","about",true,Cyan)
    )
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Night,DeepNavy)))) {
        LazyVerticalGrid(
            columns=GridCells.Fixed(2),
            modifier=Modifier.fillMaxSize(),
            horizontalArrangement=Arrangement.spacedBy(12.dp),
            verticalArrangement=Arrangement.spacedBy(12.dp),
            contentPadding=PaddingValues(start=18.dp,end=18.dp,top=22.dp,bottom=32.dp)
        ) {
            item(span={GridItemSpan(maxLineSpan)}) {
                Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                    VisionMark(Modifier.size(62.dp))
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text("VisionStudio",fontSize=32.sp,fontWeight=FontWeight.ExtraBold,color=TextPrimary)
                        Text("معالجة الصور والرؤية الحاسوبية",fontSize=14.sp,color=Cyan)
                    }
                }
            }
            item(span={GridItemSpan(maxLineSpan)}) {
                Card(shape=RoundedCornerShape(24.dp),colors=CardDefaults.cardColors(containerColor=PanelSoft)) {
                    Row(Modifier.fillMaxWidth().padding(18.dp),verticalAlignment=Alignment.CenterVertically) {
                        Box(Modifier.size(10.dp).background(Success,androidx.compose.foundation.shape.CircleShape))
                        Spacer(Modifier.width(11.dp))
                        Column {
                            Text("مختبر رؤية متكامل داخل هاتفك",fontWeight=FontWeight.Bold,color=TextPrimary,fontSize=17.sp)
                            Spacer(Modifier.height(4.dp))
                            Text("OpenCV • CameraX • YOLO • Offline",color=TextSecondary,fontSize=12.sp)
                        }
                    }
                }
            }
            item(span={GridItemSpan(maxLineSpan)}) { Text("اختر أداة",fontSize=22.sp,fontWeight=FontWeight.Bold,color=TextPrimary,modifier=Modifier.padding(top=5.dp)) }
            items(features){ f -> FeatureCard(f.icon,f.title,f.subtitle,f.accent){if(f.ready)onNavigate(f.route)else onPending(f.title)} }
        }
    }
}
