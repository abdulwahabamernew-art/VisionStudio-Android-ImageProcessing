package com.visionstudio.app

import android.app.Application
import org.opencv.android.OpenCVLoader

class VisionStudioApplication:Application(){
    companion object {@Volatile var openCvReady=false;private set}
    override fun onCreate(){
        super.onCreate()
        openCvReady=runCatching{
            OpenCVLoader.initLocal() || run { System.loadLibrary("opencv_java4");true }
        }.getOrDefault(false)
    }
}
