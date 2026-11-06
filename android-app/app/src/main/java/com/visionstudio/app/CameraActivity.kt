package com.visionstudio.app

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.provider.MediaStore
import android.util.Size
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.visionstudio.app.databinding.ActivityCameraBinding
import java.util.concurrent.Executors

class CameraActivity:AppCompatActivity(){
 private lateinit var b:ActivityCameraBinding
 private val executor=Executors.newSingleThreadExecutor()
 private var provider:ProcessCameraProvider?=null
 @Volatile private var enabled=true
 @Volatile private var selectedFilter="Original"
 @Volatile private var strength=.5f
 @Volatile private var interval=3
 @Volatile private var front=false
 @Volatile private var last:Bitmap?=null
 private var frame=0
 private val filters=listOf("Original","Grayscale","Negative","Average Blur","Gaussian Blur","Median Blur","Sharpening","Sobel","Laplacian","Canny")

 override fun onCreate(state:Bundle?){
  super.onCreate(state);b=ActivityCameraBinding.inflate(layoutInflater);setContentView(b.root)
  if(!VisionStudioApplication.openCvReady){b.status.text="تعذر تحميل OpenCV";return}
  b.previewView.scaleType=PreviewView.ScaleType.FILL_CENTER
  b.filterSpinner.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,filters)
  b.filterSpinner.onItemSelectedListener=object:AdapterView.OnItemSelectedListener{
   override fun onItemSelected(p:AdapterView<*>?,v:View?,position:Int,id:Long){selectedFilter=filters[position]}
   override fun onNothingSelected(p:AdapterView<*>?){selectedFilter="Original"}
  }
  b.filterStrength.setOnSeekBarChangeListener(seek{strength=it/100f})
  b.frameInterval.setOnSeekBarChangeListener(seek{interval=it+1;b.status.text="${selectedFilter} • كل $interval إطار"})
  b.toggleProcess.setOnClickListener{enabled=!enabled;b.processedOverlay.visibility=if(enabled)View.VISIBLE else View.GONE;b.status.text=if(enabled)"المعالجة المباشرة تعمل" else "المعالجة متوقفة"}
  b.switchCamera.setOnClickListener{front=!front;bindCamera()}
  b.captureButton.setOnClickListener{last?.let(::save)?:run{b.status.text="انتظر ظهور أول إطار"}}
  if(ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED)openCamera() else ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.CAMERA),7)
 }

 private fun seek(action:(Int)->Unit)=object:SeekBar.OnSeekBarChangeListener{
  override fun onProgressChanged(s:SeekBar?,p:Int,user:Boolean)=action(p)
  override fun onStartTrackingTouch(s:SeekBar?){};override fun onStopTrackingTouch(s:SeekBar?){}
 }
 override fun onRequestPermissionsResult(r:Int,p:Array<out String>,g:IntArray){super.onRequestPermissionsResult(r,p,g);if(r==7&&g.firstOrNull()==PackageManager.PERMISSION_GRANTED)openCamera()else b.status.text="يلزم إذن الكاميرا"}
 private fun openCamera(){val future=ProcessCameraProvider.getInstance(this);future.addListener({provider=future.get();bindCamera()},ContextCompat.getMainExecutor(this))}

 private fun bindCamera(){
  val cameraProvider=provider?:return
  val selector=if(front)CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
  val preview=Preview.Builder().build().also{it.setSurfaceProvider(b.previewView.surfaceProvider)}
  @Suppress("DEPRECATION") val analysis=ImageAnalysis.Builder().setTargetResolution(Size(1280,720)).setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
  analysis.setAnalyzer(executor){image->
   try{
    if(enabled&&frame++%interval==0){
     val raw=image.toBitmap();val oriented=orient(raw,image.imageInfo.rotationDegrees,front);if(oriented!==raw)raw.recycle()
     val out=if(selectedFilter=="Original")oriented else ImageProcessor.apply(oriented,selectedFilter,strength)
     if(out!==oriented)oriented.recycle();val previous=last;last=out
     runOnUiThread{b.processedOverlay.setImageBitmap(out);if(previous!==out)previous?.recycle();b.status.text="$selectedFilter • مباشر"}
    }
   }catch(t:Throwable){runOnUiThread{b.status.text="خطأ الكاميرا: ${t.message}"}}finally{image.close()}
  }
  runCatching{cameraProvider.unbindAll();cameraProvider.bindToLifecycle(this,selector,preview,analysis)}.onFailure{b.status.text="تعذر فتح الكاميرا: ${it.message}"}
 }
 private fun orient(src:Bitmap,degrees:Int,mirror:Boolean):Bitmap{
  if(degrees%360==0&&!mirror)return src
  val matrix=Matrix().apply{postRotate(degrees.toFloat());if(mirror)postScale(-1f,1f)}
  return Bitmap.createBitmap(src,0,0,src.width,src.height,matrix,true)
 }
 private fun save(bmp:Bitmap){
  val values=ContentValues().apply{put(MediaStore.Images.Media.DISPLAY_NAME,"VisionStudio_Live_${System.currentTimeMillis()}.jpg");put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg");put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/VisionStudio")}
  runCatching{val uri=contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values)?:error("تعذر إنشاء الملف");contentResolver.openOutputStream(uri)?.use{bmp.compress(Bitmap.CompressFormat.JPEG,95,it)}?:error("تعذر فتح الملف")}.onSuccess{b.status.text="تم حفظ اللقطة المفلترة"}.onFailure{b.status.text="فشل الحفظ: ${it.message}"}
 }
 override fun onDestroy(){provider?.unbindAll();executor.shutdown();super.onDestroy()}
}
