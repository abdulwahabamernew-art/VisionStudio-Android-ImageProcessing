package com.visionstudio.app.ui.image

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.visionstudio.app.*
import com.visionstudio.app.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.opencv.android.OpenCVLoader

@Composable private fun Header(title:String,onBack:()->Unit){Row(verticalAlignment=Alignment.CenterVertically){TextButton(onClick=onBack){Text("رجوع ←",color=Cyan)};Spacer(Modifier.weight(1f));Text(title,fontSize=24.sp,fontWeight=FontWeight.Bold,color=TextPrimary)}}

@Composable private fun rememberImagePicker(onBitmap:(Bitmap)->Unit):()->Unit{
    val context=LocalContext.current
    val launcher=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){u->u?.let{uri->runCatching{
        val bounds=BitmapFactory.Options().apply{inJustDecodeBounds=true}
        context.contentResolver.openInputStream(uri)?.use{BitmapFactory.decodeStream(it,null,bounds)}
        var sample=1;while(bounds.outWidth/sample>2048||bounds.outHeight/sample>2048)sample*=2
        val options=BitmapFactory.Options().apply{inSampleSize=sample;inPreferredConfig=Bitmap.Config.ARGB_8888}
        var bmp=context.contentResolver.openInputStream(uri)?.use{BitmapFactory.decodeStream(it,null,options)}?:error("تعذر قراءة الصورة")
        val rotation=context.contentResolver.openInputStream(uri)?.use{input->when(ExifInterface(input).getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL)){ExifInterface.ORIENTATION_ROTATE_90->90f;ExifInterface.ORIENTATION_ROTATE_180->180f;ExifInterface.ORIENTATION_ROTATE_270->270f;else->0f}}?:0f
        if(rotation!=0f){val rotated=Bitmap.createBitmap(bmp,0,0,bmp.width,bmp.height,Matrix().apply{postRotate(rotation)},true);if(rotated!==bmp)bmp.recycle();bmp=rotated}
        onBitmap(bmp)
    }}}
    
    return {launcher.launch("image/*")}
}

@Composable private fun Preview(bitmap:Bitmap?){Card(Modifier.fillMaxWidth().height(310.dp),shape=RoundedCornerShape(20.dp),colors=CardDefaults.cardColors(containerColor=Panel)){if(bitmap==null)Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){Text("اختر صورة",color=TextSecondary)}else Image(bitmap.asImageBitmap(),null,Modifier.fillMaxSize(),contentScale=ContentScale.Fit)}}

@Composable fun ImageEditorScreen(onBack:()->Unit){
    val context=LocalContext.current;val scope=rememberCoroutineScope();var original by remember{mutableStateOf<Bitmap?>(null)};var result by remember{mutableStateOf<Bitmap?>(null)};var status by remember{mutableStateOf("اختر صورة للتحرير")};var amount by remember{mutableFloatStateOf(.5f)}
    val pick=rememberImagePicker{original=it;result=it;status="تم تحميل الصورة"}
    fun apply(e:String){val input=result?:return;scope.launch{runCatching{withContext(Dispatchers.Default){ImageProcessor.apply(input,e,amount)}}.onSuccess{result=it;status="تم تطبيق $e"}.onFailure{status="فشل $e: ${it.message}"}}}
    Column(Modifier.fillMaxSize().background(Night).verticalScroll(rememberScrollState()).padding(16.dp)){Header("معالجة صورة",onBack);Text("تحرير وتحسين وحفظ",color=TextSecondary);Button(onClick=pick,modifier=Modifier.fillMaxWidth()){Text(if(original==null)"اختيار صورة" else "اختيار صورة أخرى")};Spacer(Modifier.height(12.dp));Preview(result);Spacer(Modifier.height(12.dp));Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)){ImageProcessor.edits.forEach{e->AssistChip(onClick={apply(e)},label={Text(e)})}};Text("قيمة التحرير: ${(amount*100).toInt()}%",color=TextSecondary);Slider(amount,{amount=it});Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Button({result?.let{saveBitmap(context,it,"Edit")};status="تم الحفظ"},enabled=result!=null){Text("حفظ")};OutlinedButton({result=original;status="تمت استعادة الأصل"},enabled=original!=null){Text("استعادة الأصل")}};Text(status,color=TextSecondary,modifier=Modifier.padding(vertical=12.dp))}
}

@Composable fun FilterLabScreen(onBack:()->Unit){
    val context=LocalContext.current;val scope=rememberCoroutineScope();var original by remember{mutableStateOf<Bitmap?>(null)};var result by remember{mutableStateOf<Bitmap?>(null)};var effect by remember{mutableStateOf("Gaussian Blur")};var amount by remember{mutableFloatStateOf(.5f)};var split by remember{mutableFloatStateOf(.5f)};var status by remember{mutableStateOf("اختر صورة ثم فلتر")}
    val pick=rememberImagePicker{original=it;result=it;status="تم تحميل الصورة"}
    LaunchedEffect(effect,amount,original){val b=original?:return@LaunchedEffect;delay(180);runCatching{withContext(Dispatchers.Default){ImageProcessor.apply(b,effect,amount)}}.onSuccess{result=it;status="اكتملت مقارنة $effect"}.onFailure{status="فشل $effect: ${it.message}"}}
    Column(Modifier.fillMaxSize().background(Night).verticalScroll(rememberScrollState()).padding(16.dp)){Header("مختبر الفلاتر",onBack);Text("مقارنة قبل وبعد لكل فلتر",color=TextSecondary);Button(onClick=pick,modifier=Modifier.fillMaxWidth()){Text(if(original==null)"اختيار صورة" else "اختيار صورة أخرى")};Spacer(Modifier.height(12.dp));
        BoxWithConstraints(Modifier.fillMaxWidth().height(310.dp).clip(RoundedCornerShape(20.dp)).background(Panel)){val comparisonWidth=maxWidth;if(original!=null&&result!=null){Image(result!!.asImageBitmap(),null,Modifier.fillMaxSize(),contentScale=ContentScale.Fit);Box(Modifier.fillMaxHeight().width(comparisonWidth*split).clip(RoundedCornerShape(topStart=20.dp,bottomStart=20.dp))){Image(original!!.asImageBitmap(),null,Modifier.width(comparisonWidth).fillMaxHeight(),contentScale=ContentScale.Fit)};Text("قبل",Modifier.align(Alignment.TopStart).background(Panel).padding(8.dp),color=TextPrimary);Text("بعد",Modifier.align(Alignment.TopEnd).background(Panel).padding(8.dp),color=TextPrimary)}else Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){Text("اختر صورة",color=TextSecondary)}}
        Text("موضع المقارنة",color=TextSecondary);Slider(split,{split=it},valueRange=.05f.. .95f);Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)){ImageProcessor.filters.forEach{f->FilterChip(selected=effect==f,onClick={effect=f},label={Text(f)})}};Text("$effect • القيمة ${(amount*100).toInt()}%",color=Cyan,fontWeight=FontWeight.Bold);Slider(amount,{amount=it});Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Button({result?.let{saveBitmap(context,it,effect)};status="تم حفظ نتيجة $effect"},enabled=result!=null){Text("حفظ النتيجة")};OutlinedButton({amount=.5f},enabled=original!=null){Text("القيمة الافتراضية")}};Text(status,color=TextSecondary,modifier=Modifier.padding(vertical=12.dp))}
}

@Composable fun DetectionScreen(onBack:()->Unit){
    val context=LocalContext.current;val scope=rememberCoroutineScope();val yolo=remember{YoloDetector(context.applicationContext)};var original by remember{mutableStateOf<Bitmap?>(null)};var result by remember{mutableStateOf<Bitmap?>(null)};var detections by remember{mutableStateOf(emptyList<Detection>())};var confidence by remember{mutableFloatStateOf(.30f)};var iou by remember{mutableFloatStateOf(.45f)};var busy by remember{mutableStateOf(false)};var status by remember{mutableStateOf("اختر صورة للكشف")}
    val pick=rememberImagePicker{original=it;result=it;detections=emptyList();status="الصورة جاهزة للكشف"}
    fun runYolo(){val b=original?:return;scope.launch{busy=true;runCatching{withContext(Dispatchers.Default){yolo.detect(b,confidence,iou)}}.onSuccess{result=it.bitmap;detections=it.detections;status=it.message}.onFailure{status="فشل YOLO: ${it.message}"};busy=false}}
    fun runHaar(){val b=original?:return;scope.launch{busy=true;runCatching{withContext(Dispatchers.Default){HaarDetector.detect(context,b)}}.onSuccess{result=it.bitmap;status=it.message}.onFailure{status="فشل Haar: ${it.message}"};busy=false}}
    fun isolate(){val b=original?:return;val d=detections.maxByOrNull{it.score}?:run{status="لا يوجد جسم لعزله";return};val l=d.box.left.toInt().coerceIn(0,b.width-1);val t=d.box.top.toInt().coerceIn(0,b.height-1);val r=d.box.right.toInt().coerceIn(l+1,b.width);val bot=d.box.bottom.toInt().coerceIn(t+1,b.height);result=Bitmap.createBitmap(b,l,t,r-l,bot-t);status="تم عزل الجسم الأعلى ثقة"}
    Column(Modifier.fillMaxSize().background(Night).verticalScroll(rememberScrollState()).padding(16.dp)){Header("كشف الأجسام",onBack);Text("YOLO محلي • دون فلاتر أو تحرير مكرر",color=TextSecondary);Button(onClick=pick,modifier=Modifier.fillMaxWidth()){Text(if(original==null)"اختيار صورة" else "اختيار صورة أخرى")};Spacer(Modifier.height(12.dp));Preview(result);Text("Confidence: ${"%.2f".format(confidence)}",color=TextSecondary);Slider(confidence,{confidence=it},valueRange=.10f.. .90f);Text("IoU: ${"%.2f".format(iou)}",color=TextSecondary);Slider(iou,{iou=it},valueRange=.10f.. .90f);Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Button({runYolo()},enabled=original!=null&&!busy,colors=ButtonDefaults.buttonColors(containerColor=Violet)){Text("تشغيل YOLO")};OutlinedButton({runHaar()},enabled=original!=null&&!busy){Text("وجه وعين")}};Text("الأجسام المكتشفة: ${detections.size}",color=Cyan,fontWeight=FontWeight.Bold);detections.forEachIndexed{i,d->Text("${i+1}. الفئة ${d.cls} • ${(d.score*100).toInt()}%",color=TextPrimary)};Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Button({isolate()},enabled=detections.isNotEmpty()){Text("عزل الأعلى ثقة")};Button({result?.let{saveBitmap(context,it,"Detection")};status="تم الحفظ"},enabled=result!=null){Text("حفظ")}};if(busy)LinearProgressIndicator(Modifier.fillMaxWidth());Text(status,color=if(status.startsWith("فشل"))MaterialTheme.colorScheme.error else TextSecondary,modifier=Modifier.padding(vertical=12.dp))}
}

private fun saveBitmap(context:Context,bmp:Bitmap,tag:String):Uri{val safe=tag.replace(" ","_");val v=ContentValues().apply{put(MediaStore.Images.Media.DISPLAY_NAME,"VisionStudio_${safe}_${System.currentTimeMillis()}.jpg");put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg");put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/VisionStudio")};val u=context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,v)?:error("تعذر إنشاء الصورة");context.contentResolver.openOutputStream(u)!!.use{bmp.compress(Bitmap.CompressFormat.JPEG,95,it)};return u}
