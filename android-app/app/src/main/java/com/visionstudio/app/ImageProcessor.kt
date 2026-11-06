package com.visionstudio.app

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc

object ImageProcessor {
    val filters=listOf("Grayscale","Negative","Threshold","Binary","Otsu","Average Blur","Gaussian Blur","Median Blur","Bilateral","Sharpening","Sobel","Laplacian","Canny","Histogram Equalization","Opening","Closing")
    val edits=listOf("Rotate 90","Flip","Resize 50%","Center Crop","Brightness","Contrast")

    fun apply(bitmap:Bitmap,effect:String,amount:Float=.5f):Bitmap{
        check(VisionStudioApplication.openCvReady){"OpenCV غير محمّل"}
        val rgba=Mat();val src=Mat();val out=Mat()
        try{
            Utils.bitmapToMat(bitmap.copy(Bitmap.Config.ARGB_8888,false),rgba)
            Imgproc.cvtColor(rgba,src,Imgproc.COLOR_RGBA2RGB)
            val value=amount.coerceIn(0f,1f)
            val odd=((3+(value*12).toInt())/2*2+1).coerceIn(3,15)
            when(effect){
                "Grayscale"->gray(src,out)
                "Negative"->Core.bitwise_not(src,out)
                "Threshold","Binary"->{gray(src,out);Imgproc.threshold(out,out,(value*255).toDouble(),255.0,Imgproc.THRESH_BINARY)}
                "Otsu"->{gray(src,out);Imgproc.threshold(out,out,0.0,255.0,Imgproc.THRESH_BINARY+Imgproc.THRESH_OTSU)}
                "Average Blur"->Imgproc.blur(src,out,Size(odd.toDouble(),odd.toDouble()))
                "Gaussian Blur"->Imgproc.GaussianBlur(src,out,Size(odd.toDouble(),odd.toDouble()),0.0)
                "Median Blur"->Imgproc.medianBlur(src,out,odd)
                "Bilateral"->{val sigma=25.0+value*125.0;Imgproc.bilateralFilter(src,out,odd,sigma,sigma)}
                "Sharpening"->{val strength=.3+value*1.7;val k=Mat(3,3,CvType.CV_32F);try{k.put(0,0,0.0,-strength,0.0,-strength,1.0+4*strength,-strength,0.0,-strength,0.0);Imgproc.filter2D(src,out,-1,k)}finally{k.release()}}
                "Sobel"->{val g=Mat();val x=Mat();val y=Mat();val ax=Mat();val ay=Mat();try{gray(src,g);Imgproc.Sobel(g,x,CvType.CV_16S,1,0,3);Imgproc.Sobel(g,y,CvType.CV_16S,0,1,3);Core.convertScaleAbs(x,ax);Core.convertScaleAbs(y,ay);Core.addWeighted(ax,.5,ay,.5,0.0,out)}finally{listOf(g,x,y,ax,ay).forEach{it.release()}}}
                "Laplacian"->{val g=Mat();val l=Mat();try{gray(src,g);Imgproc.Laplacian(g,l,CvType.CV_16S,odd.coerceAtMost(7));Core.convertScaleAbs(l,out)}finally{g.release();l.release()}}
                "Canny"->{gray(src,out);val low=20.0+value*180.0;Imgproc.Canny(out,out,low,low*2.0)}
                "Histogram Equalization"->{gray(src,out);Imgproc.equalizeHist(out,out)}
                "Opening","Closing"->{val g=Mat();val k=Imgproc.getStructuringElement(Imgproc.MORPH_RECT,Size(odd.toDouble(),odd.toDouble()));try{gray(src,g);Imgproc.morphologyEx(g,out,if(effect=="Opening")Imgproc.MORPH_OPEN else Imgproc.MORPH_CLOSE,k)}finally{g.release();k.release()}}
                "Rotate 90"->Core.rotate(src,out,Core.ROTATE_90_CLOCKWISE)
                "Flip"->Core.flip(src,out,1)
                "Resize 50%"->Imgproc.resize(src,out,Size((src.cols()/2.0).coerceAtLeast(1.0),(src.rows()/2.0).coerceAtLeast(1.0)),0.0,0.0,Imgproc.INTER_AREA)
                "Center Crop"->{val w=(src.cols()*3/4).coerceAtLeast(1);val h=(src.rows()*3/4).coerceAtLeast(1);src.submat(Rect((src.cols()-w)/2,(src.rows()-h)/2,w,h)).use{it.copyTo(out)}}
                "Brightness"->src.convertTo(out,-1,1.0,(value-.5)*160.0)
                "Contrast"->src.convertTo(out,-1,.4+value*1.8,0.0)
                else->src.copyTo(out)
            }
            check(!out.empty()){"لم ينتج الفلتر صورة"}
            return Bitmap.createBitmap(out.cols(),out.rows(),Bitmap.Config.ARGB_8888).also{Utils.matToBitmap(out,it)}
        }finally{rgba.release();src.release();out.release()}
    }
    private fun gray(src:Mat,out:Mat)=Imgproc.cvtColor(src,out,Imgproc.COLOR_RGB2GRAY)
    private inline fun <T> Mat.use(block:(Mat)->T):T=try{block(this)}finally{release()}
}
