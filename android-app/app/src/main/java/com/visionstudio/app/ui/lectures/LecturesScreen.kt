package com.visionstudio.app.ui.lectures

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalLayoutDirection
import org.json.JSONObject

private val Night = Color(0xFF07111F)
private val Card = Color(0xFF12263D)
private val Cyan = Color(0xFF2ED3E3)
private val Soft = Color(0xFFAABAD0)

data class CourseSection(val title:String,val code:String,val explanation:String,val parameters:String,val result:String)
data class CourseLab(val number:Int,val title:String,val file:String,val sections:List<CourseSection>)

private fun loadLabs(text:String):List<CourseLab>{
    val array=JSONObject(text).getJSONArray("labs")
    return (0 until array.length()).map{i->
        val item=array.getJSONObject(i)
        val sections=item.getJSONArray("sections")
        CourseLab(item.getInt("number"),item.getString("title"),item.getString("file"),
            (0 until sections.length()).map{j->val s=sections.getJSONObject(j);CourseSection(s.getString("title"),s.getString("code"),s.optString("explanation","شرح غير متاح"),s.optString("parameters","لا توجد معاملات إضافية"),s.optString("result","يعرض أو يحفظ ناتج العملية"))})
    }
}

@Composable
fun LecturesScreen(onBack:()->Unit){
    val context=LocalContext.current
    val labs=remember{runCatching{loadLabs(context.assets.open("course_labs/catalog.json").bufferedReader().use{it.readText()})}.getOrElse{emptyList()}}
    var selected by remember{mutableStateOf<CourseLab?>(null)}
    var sectionIndex by remember{mutableStateOf(0)}
    if(selected==null){
        LazyColumn(Modifier.fillMaxSize().background(Night).padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
            item{
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){
                    Text("محاضرات المقرر",color=Color.White,fontSize=29.sp,fontWeight=FontWeight.Bold)
                    Text("رجوع ←",color=Cyan,fontSize=18.sp,modifier=Modifier.clickable{onBack()}.padding(8.dp))
                }
                Text("10 Labs • Python / OpenCV • يعمل دون إنترنت",color=Soft,fontSize=15.sp)
                Spacer(Modifier.height(8.dp))
            }
            if(labs.isEmpty()) item{Text("تعذر قراءة ملفات المحاضرات",color=Color(0xFFFF8A80))}
            items(labs){lab->
                Card(colors=CardDefaults.cardColors(containerColor=Card),shape=RoundedCornerShape(22.dp),modifier=Modifier.fillMaxWidth().clickable{selected=lab;sectionIndex=0}){
                    Column(Modifier.padding(18.dp)){
                        Text("LAB ${lab.number}",color=Cyan,fontWeight=FontWeight.Bold,fontSize=16.sp)
                        Spacer(Modifier.height(5.dp))
                        Text(lab.title,color=Color.White,fontWeight=FontWeight.Bold,fontSize=20.sp)
                        Text("${lab.sections.size} أقسام برمجية • اضغط لعرض الكود",color=Soft,fontSize=14.sp)
                    }
                }
            }
            item{Spacer(Modifier.height(24.dp))}
        }
    }else{
        val lab=selected!!
        val safeIndex=sectionIndex.coerceIn(0,(lab.sections.size-1).coerceAtLeast(0))
        val section=lab.sections.getOrNull(safeIndex)
        val clipboard=LocalClipboardManager.current
        LazyColumn(Modifier.fillMaxSize().background(Night).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
            item{
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){
                    Column(Modifier.weight(1f)){
                        Text("LAB ${lab.number}",color=Cyan,fontWeight=FontWeight.Bold)
                        Text(lab.title,color=Color.White,fontSize=24.sp,fontWeight=FontWeight.Bold)
                    }
                    Text("رجوع ←",color=Cyan,fontSize=18.sp,modifier=Modifier.clickable{selected=null}.padding(8.dp))
                }
                Text("الكود مرجع Python تعليمي، والتنفيذ داخل التطبيق يستخدم Kotlin/OpenCV.",color=Soft,fontSize=14.sp)
            }
            item{
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)){
                    lab.sections.forEachIndexed{i,s->
                        FilterChip(selected=i==safeIndex,onClick={sectionIndex=i},label={Text(s.title,maxLines=1)})
                    }
                }
            }
            item{
                Card(colors=CardDefaults.cardColors(containerColor=Card),shape=RoundedCornerShape(18.dp),modifier=Modifier.fillMaxWidth()){
                    Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(9.dp)){
                        Text("شرح الكود",color=Cyan,fontWeight=FontWeight.Bold,fontSize=18.sp)
                        Text(section?.explanation?:"",color=Color.White,fontSize=15.sp,lineHeight=22.sp)
                        Text("المعاملات المهمة",color=Cyan,fontWeight=FontWeight.Bold,fontSize=16.sp)
                        Text(section?.parameters?:"",color=Soft,fontSize=14.sp,lineHeight=21.sp)
                        Text("النتيجة المتوقعة",color=Cyan,fontWeight=FontWeight.Bold,fontSize=16.sp)
                        Text(section?.result?:"",color=Soft,fontSize=14.sp,lineHeight=21.sp)
                    }
                }
            }
            item{
                Button(onClick={if(section!=null){clipboard.setText(AnnotatedString(section.code));Toast.makeText(context,"تم نسخ الكود",Toast.LENGTH_SHORT).show()}},enabled=section!=null,colors=ButtonDefaults.buttonColors(containerColor=Cyan,contentColor=Night),modifier=Modifier.fillMaxWidth()){
                    Text("نسخ الكود",fontWeight=FontWeight.Bold)
                }
            }
            item{
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr){
                    androidx.compose.foundation.text.selection.SelectionContainer{
                        Text(section?.code?:"لا يوجد كود",color=Color(0xFFE6EDF7),fontFamily=FontFamily.Monospace,fontSize=13.sp,lineHeight=19.sp,modifier=Modifier.fillMaxWidth().background(Color(0xFF0B1726),RoundedCornerShape(16.dp)).padding(15.dp))
                    }
                }
            }
            item{Spacer(Modifier.height(30.dp))}
        }
    }
}
