package com.visionstudio.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.visionstudio.app.ui.theme.*

enum class ToolIcon { Camera, Image, Video, Filters, Detect, Results, About }

@Composable
fun VisionMark(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w=size.width; val h=size.height; val c=Offset(w/2,h/2)
        drawRoundRect(brush=Brush.linearGradient(listOf(ElectricBlue,Violet)),cornerRadius=androidx.compose.ui.geometry.CornerRadius(w*.24f,w*.24f))
        drawCircle(color=Night,radius=w*.28f,center=c)
        drawCircle(color=Cyan,radius=w*.18f,center=c,style=Stroke(w*.055f))
        drawCircle(color=Color.White,radius=w*.055f,center=c)
        val s=w*.16f; val sw=w*.035f
        drawLine(Color.White,Offset(w*.16f,h*.31f),Offset(w*.16f+s,h*.31f),sw)
        drawLine(Color.White,Offset(w*.16f,h*.31f),Offset(w*.16f,h*.31f+s),sw)
        drawLine(Color.White,Offset(w*.84f-s,h*.69f),Offset(w*.84f,h*.69f),sw)
        drawLine(Color.White,Offset(w*.84f,h*.69f-s),Offset(w*.84f,h*.69f),sw)
    }
}

@Composable
private fun ToolGlyph(type:ToolIcon, modifier:Modifier=Modifier) {
    Canvas(modifier) {
        val p=Paint().apply{color=Cyan}; val stroke=Stroke(width=size.width*.075f,cap=StrokeCap.Round,join=StrokeJoin.Round)
        val w=size.width; val h=size.height
        when(type){
            ToolIcon.Camera -> { drawRoundRect(Cyan,Offset(w*.12f,h*.25f),Size(w*.76f,h*.58f),style=stroke); drawCircle(color=Cyan,radius=w*.17f,center=Offset(w*.5f,h*.54f),style=stroke); drawLine(Cyan,Offset(w*.31f,h*.25f),Offset(w*.4f,h*.12f),stroke.width); drawLine(Cyan,Offset(w*.4f,h*.12f),Offset(w*.6f,h*.12f),stroke.width) }
            ToolIcon.Image -> { drawRoundRect(Cyan,Offset(w*.12f,h*.12f),Size(w*.76f,h*.76f),style=stroke); drawCircle(color=Cyan,radius=w*.08f,center=Offset(w*.67f,h*.34f)); drawLine(Cyan,Offset(w*.2f,h*.75f),Offset(w*.42f,h*.5f),stroke.width); drawLine(Cyan,Offset(w*.42f,h*.5f),Offset(w*.58f,h*.66f),stroke.width); drawLine(Cyan,Offset(w*.58f,h*.66f),Offset(w*.72f,h*.52f),stroke.width) }
            ToolIcon.Video -> { drawRoundRect(Cyan,Offset(w*.1f,h*.22f),Size(w*.58f,h*.56f),style=stroke); val path=Path().apply{moveTo(w*.72f,h*.38f);lineTo(w*.91f,h*.27f);lineTo(w*.91f,h*.73f);lineTo(w*.72f,h*.62f);close()};drawPath(path,Cyan,style=stroke) }
            ToolIcon.Filters -> { for(i in 0..2){val y=h*(.25f+i*.25f);drawLine(Cyan,Offset(w*.12f,y),Offset(w*.88f,y),stroke.width);drawCircle(color=Cyan,radius=w*.09f,center=Offset(w*(if(i%2==0).38f else .68f),y)) } }
            ToolIcon.Detect -> { drawLine(Cyan,Offset(w*.12f,h*.34f),Offset(w*.12f,h*.12f),stroke.width);drawLine(Cyan,Offset(w*.12f,h*.12f),Offset(w*.34f,h*.12f),stroke.width);drawLine(Cyan,Offset(w*.66f,h*.12f),Offset(w*.88f,h*.12f),stroke.width);drawLine(Cyan,Offset(w*.88f,h*.12f),Offset(w*.88f,h*.34f),stroke.width);drawLine(Cyan,Offset(w*.12f,h*.66f),Offset(w*.12f,h*.88f),stroke.width);drawLine(Cyan,Offset(w*.12f,h*.88f),Offset(w*.34f,h*.88f),stroke.width);drawLine(Cyan,Offset(w*.66f,h*.88f),Offset(w*.88f,h*.88f),stroke.width);drawLine(Cyan,Offset(w*.88f,h*.66f),Offset(w*.88f,h*.88f),stroke.width);drawCircle(color=Cyan,radius=w*.16f,center=Offset(w*.5f,h*.5f),style=stroke) }
            ToolIcon.Results -> { drawRoundRect(Cyan,Offset(w*.18f,h*.12f),Size(w*.64f,h*.76f),style=stroke);drawLine(Cyan,Offset(w*.32f,h*.4f),Offset(w*.7f,h*.4f),stroke.width);drawLine(Cyan,Offset(w*.32f,h*.58f),Offset(w*.7f,h*.58f),stroke.width);drawLine(Cyan,Offset(w*.32f,h*.76f),Offset(w*.58f,h*.76f),stroke.width) }
            ToolIcon.About -> { drawCircle(color=Cyan,radius=w*.38f,center=Offset(w*.5f,h*.5f),style=stroke);drawCircle(color=Cyan,radius=w*.045f,center=Offset(w*.5f,h*.31f));drawLine(Cyan,Offset(w*.5f,h*.47f),Offset(w*.5f,h*.7f),stroke.width) }
        }
    }
}

@Composable
fun FeatureCard(icon:ToolIcon,title:String,subtitle:String,accent:Color,onClick:()->Unit) {
    Card(
        modifier=Modifier.fillMaxWidth().height(154.dp).clickable(onClick=onClick),
        shape=RoundedCornerShape(24.dp),
        colors=CardDefaults.cardColors(containerColor=Panel),
        border=BorderStroke(1.dp,AppStroke),
        elevation=CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.fillMaxSize().padding(17.dp),verticalArrangement=Arrangement.SpaceBetween) {
            Box(Modifier.size(46.dp),contentAlignment=Alignment.Center){
                Surface(Modifier.fillMaxSize(),shape=RoundedCornerShape(14.dp),color=accent.copy(alpha=.14f)){}
                ToolGlyph(icon,Modifier.size(27.dp))
            }
            Column {
                Text(title,fontSize=18.sp,fontWeight=FontWeight.Bold,color=TextPrimary)
                Spacer(Modifier.height(4.dp))
                Text(subtitle,fontSize=12.sp,lineHeight=17.sp,color=TextSecondary)
            }
        }
    }
}
