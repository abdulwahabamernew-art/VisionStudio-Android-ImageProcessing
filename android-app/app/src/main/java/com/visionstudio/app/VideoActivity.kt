package com.visionstudio.app

import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.visionstudio.app.databinding.ActivityVideoBinding
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class VideoActivity:AppCompatActivity(){
 private lateinit var b:ActivityVideoBinding
 private val worker=Executors.newSingleThreadExecutor()
 private val playing=AtomicBoolean(false)
 @Volatile private var uri:Uri?=null
 @Volatile private var selectedFilter="Original"
 @Volatile private var strength=.5f
 @Volatile private var durationUs=0L
 @Volatile private var positionUs=0L
 @Volatile private var rotation=0
 private var displayed:Bitmap?=null
 private val filters=listOf("Original","Grayscale","Negative","Average Blur","Gaussian Blur","Median Blur","Sharpening","Sobel","Laplacian","Canny")
 private val picker=registerForActivityResult(ActivityResultContracts.GetContent()){u->u?.let{loadVideo(it)}}

 override fun onCreate(state:Bundle?){
  super.onCreate(state);b=ActivityVideoBinding.inflate(layoutInflater);setContentView(b.root)
  if(!VisionStudioApplication.openCvReady){b.status.text="تعذر تحميل OpenCV";return}
  b.filterSpinner.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,filters)
  b.filterSpinner.onItemSelectedListener=object:AdapterView.OnItemSelectedListener{
   override fun onItemSelected(p:AdapterView<*>?,v:View?,i:Int,id:Long){selectedFilter=filters[i]}
   override fun onNothingSelected(p:AdapterView<*>?){selectedFilter="Original"}
  }
  b.filterStrength.setOnSeekBarChangeListener(seek{strength=it/100f})
  b.timeline.setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener{
   override fun onProgressChanged(s:SeekBar?,p:Int,user:Boolean){if(user&&durationUs>0){positionUs=durationUs*p/1000;showSingleFrame()}}
   override fun onStartTrackingTouch(s:SeekBar?){playing.set(false)};override fun onStopTrackingTouch(s:SeekBar?){}
  })
  b.openButton.setOnClickListener{playing.set(false);picker.launch("video/*")}
  b.playButton.setOnClickListener{if(uri==null)b.status.text="اختر فيديو أولًا" else startPreview()}
  b.stopButton.setOnClickListener{playing.set(false);b.status.text="تم إيقاف المعاينة"}
 }
 private fun seek(action:(Int)->Unit)=object:SeekBar.OnSeekBarChangeListener{override fun onProgressChanged(s:SeekBar?,p:Int,u:Boolean)=action(p);override fun onStartTrackingTouch(s:SeekBar?){};override fun onStopTrackingTouch(s:SeekBar?){} }
 private fun loadVideo(value:Uri){
  playing.set(false);uri=value;b.progress.visibility=View.VISIBLE;b.status.text="قراءة الفيديو..."
  worker.execute{runCatching{val r=MediaMetadataRetriever();try{r.setDataSource(this,value);durationUs=(r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()?:0L)*1000L;rotation=r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull()?:0;require(durationUs>0){"مدة الفيديو غير صالحة"};positionUs=0;r.getFrameAtTime(0,MediaMetadataRetriever.OPTION_CLOSEST_SYNC)?:error("تعذر قراءة الفيديو")}finally{r.release()}}.onSuccess{raw->val frame=orient(raw,rotation);if(frame!==raw)raw.recycle();runOnUiThread{b.progress.visibility=View.GONE;display(frame);b.timeline.progress=0;b.status.text="جاهز • اختر الفلتر ثم تشغيل"}}.onFailure{runOnUiThread{b.progress.visibility=View.GONE;b.status.text="فشل فتح الفيديو: ${it.message}"}}}
 }
 private fun startPreview(){
  if(playing.getAndSet(true))return
  val source=uri?:return
  worker.execute{
   val retriever=MediaMetadataRetriever()
   try{
    retriever.setDataSource(this,source);var lastClock=SystemClock.elapsedRealtime()
    while(playing.get()&&positionUs<durationUs){
     val raw=retriever.getFrameAtTime(positionUs,MediaMetadataRetriever.OPTION_CLOSEST)
     if(raw==null){positionUs+=66_667;continue}
     val upright=orient(raw,rotation);if(upright!==raw)raw.recycle()
     val output=if(selectedFilter=="Original")upright else ImageProcessor.apply(upright,selectedFilter,strength)
     if(output!==upright)upright.recycle();positionUs=(positionUs+66_667).coerceAtMost(durationUs)
     val progress=(positionUs*1000/durationUs).toInt();runOnUiThread{display(output);b.timeline.progress=progress;b.status.text="$selectedFilter • معاينة مباشرة"}
     val spent=SystemClock.elapsedRealtime()-lastClock;val wait=(66-spent).coerceAtLeast(0);if(wait>0)Thread.sleep(wait);lastClock=SystemClock.elapsedRealtime()
    }
    if(positionUs>=durationUs)positionUs=0
   }catch(t:Throwable){runOnUiThread{b.status.text="فشل المعاينة: ${t.message}"}}finally{playing.set(false);retriever.release()}
  }
 }
 private fun showSingleFrame(){
  val source=uri?:return;val at=positionUs
  worker.execute{runCatching{val r=MediaMetadataRetriever();try{r.setDataSource(this,source);r.getFrameAtTime(at,MediaMetadataRetriever.OPTION_CLOSEST)?:error("تعذر قراءة الإطار")}finally{r.release()}}.onSuccess{raw->val upright=orient(raw,rotation);if(upright!==raw)raw.recycle();val output=if(selectedFilter=="Original")upright else ImageProcessor.apply(upright,selectedFilter,strength);if(output!==upright)upright.recycle();runOnUiThread{display(output)}}}
 }
 private fun display(frame:Bitmap){val old=displayed;displayed=frame;b.processedVideo.setImageBitmap(frame);if(old!==frame)old?.recycle()}
 private fun orient(src:Bitmap,degrees:Int):Bitmap{val d=((degrees%360)+360)%360;if(d==0)return src;return Bitmap.createBitmap(src,0,0,src.width,src.height,Matrix().apply{postRotate(d.toFloat())},true)}
 override fun onDestroy(){playing.set(false);worker.shutdownNow();displayed?.recycle();super.onDestroy()}
}
