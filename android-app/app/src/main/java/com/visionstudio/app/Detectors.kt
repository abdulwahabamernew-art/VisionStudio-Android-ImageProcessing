package com.visionstudio.app

import android.content.Context
import android.graphics.*
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import kotlin.math.*

data class Detection(val box:RectF,val score:Float,val cls:Int)
data class DetectionResult(val bitmap:Bitmap,val count:Int,val message:String,val detections:List<Detection> = emptyList())

object HaarDetector{
    fun detect(context:Context,bitmap:Bitmap):DetectionResult{
        fun classifier(name:String):CascadeClassifier{val dir=context.getDir("cascades",Context.MODE_PRIVATE);val f=java.io.File(dir,name);context.assets.open(name).use{input->f.outputStream().use{input.copyTo(it)}};return CascadeClassifier(f.absolutePath)}
        val faces=classifier("haarcascade_frontalface_default.xml");val eyes=classifier("haarcascade_eye.xml")
        require(!faces.empty()&&!eyes.empty()){"تعذر تحميل ملفات Haar"}
        val mat=Mat();Utils.bitmapToMat(bitmap.copy(Bitmap.Config.ARGB_8888,true),mat);val gray=Mat();Imgproc.cvtColor(mat,gray,Imgproc.COLOR_RGBA2GRAY);Imgproc.equalizeHist(gray,gray)
        val found=MatOfRect();faces.detectMultiScale(gray,found,1.1,4,0,Size(40.0,40.0));var eyeCount=0
        for(face in found.toArray()){Imgproc.rectangle(mat,face.tl(),face.br(),Scalar(39.0,215.0,232.0,255.0),4);val roi=gray.submat(face);val es=MatOfRect();eyes.detectMultiScale(roi,es,1.1,4);eyeCount+=es.toArray().size;for(e in es.toArray()){val p1=org.opencv.core.Point(face.x+e.x.toDouble(),face.y+e.y.toDouble());Imgproc.rectangle(mat,p1,org.opencv.core.Point(p1.x+e.width,p1.y+e.height),Scalar(122.0,92.0,255.0,255.0),3)};roi.release();es.release()}
        val out=Bitmap.createBitmap(mat.cols(),mat.rows(),Bitmap.Config.ARGB_8888);Utils.matToBitmap(mat,out);val faceCount=found.toArray().size;mat.release();gray.release();found.release();return DetectionResult(out,faceCount,"وجوه: $faceCount • عيون: $eyeCount")
    }
}

class YoloDetector(private val context:Context){
    private val size=640
    private val labels=runCatching{context.assets.open("coco_labels.txt").bufferedReader().readLines()}.getOrDefault(List(80){"class_$it"})
    private val interpreter by lazy{Interpreter(loadModel(),Interpreter.Options().apply{setNumThreads(4)})}
    private fun loadModel():ByteBuffer{val a=context.assets.openFd("yolo_model.tflite");return FileInputStream(a.fileDescriptor).channel.map(FileChannel.MapMode.READ_ONLY,a.startOffset,a.declaredLength)}
    fun detect(src:Bitmap,conf:Float=.25f,iouThreshold:Float=.45f):DetectionResult{
        val inputShape=interpreter.getInputTensor(0).shape();require(inputShape.contentEquals(intArrayOf(1,3,640,640))){"دخل YOLO غير مدعوم: ${inputShape.contentToString()}"}
        val outputShape=interpreter.getOutputTensor(0).shape();require(outputShape.contentEquals(intArrayOf(1,84,8400))){"خرج YOLO غير مدعوم: ${outputShape.contentToString()}"}
        val resized=Bitmap.createScaledBitmap(src,size,size,true);val pixels=IntArray(size*size);resized.getPixels(pixels,0,size,0,0,size,size);val input=ByteBuffer.allocateDirect(4*3*size*size).order(ByteOrder.nativeOrder())
        for(c in 0..2)for(p in pixels)input.putFloat((if(c==0)Color.red(p) else if(c==1)Color.green(p) else Color.blue(p))/255f)
        input.rewind();val output=Array(1){Array(84){FloatArray(8400)}};interpreter.run(input,output);val raw=mutableListOf<Detection>();val sx=src.width/size.toFloat();val sy=src.height/size.toFloat();var maximum=0f
        for(i in 0 until 8400){var cls=0;var score=0f;for(c in 0 until 80){val s=output[0][c+4][i];if(s>score){score=s;cls=c}};maximum=max(maximum,score);if(score<conf)continue;val cx=output[0][0][i];val cy=output[0][1][i];val w=output[0][2][i];val h=output[0][3][i];raw+=Detection(RectF((cx-w/2)*sx,(cy-h/2)*sy,(cx+w/2)*sx,(cy+h/2)*sy),score,cls)}
        val kept=nms(raw,iouThreshold.coerceIn(.10f,.90f));val out=src.copy(Bitmap.Config.ARGB_8888,true);val canvas=Canvas(out);val box=Paint().apply{style=Paint.Style.STROKE;strokeWidth=5f;color=Color.CYAN};val text=Paint().apply{color=Color.YELLOW;textSize=32f}
        kept.forEach{d->canvas.drawRect(d.box,box);canvas.drawText("${labels.getOrElse(d.cls){"class_${d.cls}"}} %.2f".format(d.score),d.box.left.coerceAtLeast(0f),d.box.top.coerceAtLeast(34f),text)}
        return DetectionResult(out,kept.size,if(kept.isEmpty())"لم يظهر كشف — أعلى ثقة %.3f".format(maximum) else "اكتشف YOLO ${kept.size} جسم",kept)
    }
    fun isolate(src:Bitmap):DetectionResult{val r=detect(src);if(r.count==0)return r;return r}
    private fun nms(src:List<Detection>,t:Float):List<Detection>{val q=src.sortedByDescending{it.score}.toMutableList();val out=mutableListOf<Detection>();while(q.isNotEmpty()){val a=q.removeAt(0);out+=a;q.removeAll{it.cls==a.cls&&iou(a.box,it.box)>t}};return out}
    private fun iou(a:RectF,b:RectF):Float{val l=max(a.left,b.left);val t=max(a.top,b.top);val r=min(a.right,b.right);val bot=min(a.bottom,b.bottom);val inter=max(0f,r-l)*max(0f,bot-t);return inter/(a.width()*a.height()+b.width()*b.height()-inter).coerceAtLeast(1f)}
}
