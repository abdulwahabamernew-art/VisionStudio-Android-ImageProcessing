package com.visionstudio.app

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import java.io.File
import java.nio.ByteBuffer
import java.util.concurrent.CancellationException
import kotlin.math.min
import kotlin.math.roundToInt

object VideoProcessor {
    data class EncoderChoice(val name:String,val colorFormat:Int,val semiPlanar:Boolean)

    fun process(
        context:Context,
        uri:Uri,
        effect:String,
        cancelled:()->Boolean={false},
        onProgress:(Int)->Unit={}
    ):Uri {
        val retriever=MediaMetadataRetriever()
        val temp=File(context.cacheDir,"VisionStudio_H264_${System.currentTimeMillis()}.mp4")
        var encoder:MediaCodec?=null
        var muxer:MediaMuxer?=null
        var saved:Uri?=null
        try{
            retriever.setDataSource(context,uri)
            val durationMs=retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
                ?:error("تعذر قراءة مدة الفيديو")
            require(durationMs>0){"مدة الفيديو غير صالحة"}
            val sourceFps=retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)?.toFloatOrNull()
                ?.takeIf{it in 1f..120f}?:24f
            val fps=min(sourceFps,24f).roundToInt().coerceAtLeast(1)
            val durationUs=durationMs*1000L
            val totalFrames=((durationUs*fps)/1_000_000L).toInt().coerceAtLeast(1)

            var first=retriever.getFrameAtTime(0,MediaMetadataRetriever.OPTION_CLOSEST)
                ?:error("تعذر فك أول إطار من الفيديو")
            first=scaleForExport(first)
            val width=first.width and 1.inv()
            val height=first.height and 1.inv()
            require(width>=2&&height>=2){"أبعاد الفيديو غير صالحة"}

            val choice=findAvcEncoder()
            val format=MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC,width,height).apply{
                setInteger(MediaFormat.KEY_COLOR_FORMAT,choice.colorFormat)
                setInteger(MediaFormat.KEY_BIT_RATE,(width*height*4).coerceIn(1_000_000,8_000_000))
                setInteger(MediaFormat.KEY_FRAME_RATE,fps)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,1)
            }
            encoder=MediaCodec.createByCodecName(choice.name)
            encoder.configure(format,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE)
            encoder.start()
            muxer=MediaMuxer(temp.absolutePath,MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val info=MediaCodec.BufferInfo()
            var track=-1
            var muxerStarted=false

            fun drain(end:Boolean){
                var idle=0
                while(true){
                    val index=encoder!!.dequeueOutputBuffer(info,10_000)
                    when{
                        index==MediaCodec.INFO_TRY_AGAIN_LATER->{
                            if(!end||idle++>100)return
                        }
                        index==MediaCodec.INFO_OUTPUT_FORMAT_CHANGED->{
                            check(!muxerStarted){"تغير تنسيق المشفّر أكثر من مرة"}
                            track=muxer!!.addTrack(encoder!!.outputFormat)
                            muxer!!.start();muxerStarted=true
                        }
                        index>=0->{
                            val out=encoder!!.getOutputBuffer(index)?:error("تعذر قراءة خرج المشفّر")
                            if(info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG !=0)info.size=0
                            if(info.size>0){
                                check(muxerStarted){"لم يبدأ MediaMuxer"}
                                out.position(info.offset);out.limit(info.offset+info.size)
                                muxer!!.writeSampleData(track,out,info)
                            }
                            val eos=info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM!=0
                            encoder!!.releaseOutputBuffer(index,false)
                            if(eos)return
                        }
                    }
                }
            }

            var reusableFirst:Bitmap?=first
            for(frameIndex in 0 until totalFrames){
                if(cancelled())throw CancellationException("ألغى المستخدم معالجة الفيديو")
                val timeUs=frameIndex*1_000_000L/fps
                var raw=if(frameIndex==0){reusableFirst.also{reusableFirst=null}}
                    else retriever.getFrameAtTime(timeUs,MediaMetadataRetriever.OPTION_CLOSEST)
                if(raw==null)continue
                val scaled=scaleExact(raw,width,height)
                if(scaled!==raw)raw.recycle()
                val filtered=ImageProcessor.apply(scaled,effect)
                if(filtered!==scaled)scaled.recycle()
                val exact=scaleExact(filtered,width,height)
                if(exact!==filtered)filtered.recycle()
                val yuv=bitmapToYuv420(exact,choice.semiPlanar)
                exact.recycle()

                var inputIndex:Int
                do{
                    if(cancelled())throw CancellationException("ألغى المستخدم معالجة الفيديو")
                    inputIndex=encoder!!.dequeueInputBuffer(10_000)
                    if(inputIndex<0)drain(false)
                }while(inputIndex<0)
                val input=encoder!!.getInputBuffer(inputIndex)?:error("تعذر الوصول إلى دخل المشفّر")
                input.clear();require(input.capacity()>=yuv.size){"ذاكرة المشفّر أصغر من الإطار"};input.put(yuv)
                encoder!!.queueInputBuffer(inputIndex,0,yuv.size,timeUs,0)
                drain(false)
                onProgress(((frameIndex+1)*100/totalFrames).coerceIn(0,100))
            }

            var eosIndex:Int
            do{eosIndex=encoder!!.dequeueInputBuffer(10_000);if(eosIndex<0)drain(false)}while(eosIndex<0)
            encoder!!.queueInputBuffer(eosIndex,0,0,durationUs,MediaCodec.BUFFER_FLAG_END_OF_STREAM)
            drain(true)

            encoder!!.stop();encoder!!.release();encoder=null
            if(muxerStarted)muxer!!.stop()
            muxer!!.release();muxer=null
            require(temp.exists()&&temp.length()>1024){"ملف الفيديو الناتج فارغ"}

            val values=ContentValues().apply{
                put(MediaStore.Video.Media.DISPLAY_NAME,"VisionStudio_${System.currentTimeMillis()}.mp4")
                put(MediaStore.Video.Media.MIME_TYPE,"video/mp4")
                put(MediaStore.Video.Media.RELATIVE_PATH,"Movies/VisionStudio")
                if(Build.VERSION.SDK_INT>=29)put(MediaStore.Video.Media.IS_PENDING,1)
            }
            val target=context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,values)
                ?:error("تعذر إنشاء ملف الفيديو في الهاتف")
            saved=target
            context.contentResolver.openOutputStream(target)?.use{dst->temp.inputStream().use{src->src.copyTo(dst)}}
                ?:error("تعذر فتح ملف الفيديو للحفظ")
            if(Build.VERSION.SDK_INT>=29){
                values.clear();values.put(MediaStore.Video.Media.IS_PENDING,0)
                context.contentResolver.update(target,values,null,null)
            }
            onProgress(100)
            return target
        }catch(t:Throwable){
            saved?.let{runCatching{context.contentResolver.delete(it,null,null)}}
            throw t
        }finally{
            runCatching{retriever.release()}
            runCatching{encoder?.stop()};runCatching{encoder?.release()}
            runCatching{muxer?.stop()};runCatching{muxer?.release()}
            temp.delete()
        }
    }

    private fun findAvcEncoder():EncoderChoice{
        val choices=MediaCodecList(MediaCodecList.REGULAR_CODECS).codecInfos.filter{info->
            info.isEncoder&&info.supportedTypes.any{it.equals(MediaFormat.MIMETYPE_VIDEO_AVC,true)}
        }
        for(info in choices){
            val formats=info.getCapabilitiesForType(MediaFormat.MIMETYPE_VIDEO_AVC).colorFormats.toSet()
            if(MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar in formats)
                return EncoderChoice(info.name,MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar,true)
            if(MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar in formats)
                return EncoderChoice(info.name,MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar,false)
            if(MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible in formats)
                return EncoderChoice(info.name,MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible,false)
        }
        error("الهاتف لا يوفر مشفّر H.264 يقبل إطارات YUV420")
    }

    private fun scaleForExport(bitmap:Bitmap):Bitmap{
        val maxW=1280;val maxH=720
        val ratio=min(1f,min(maxW.toFloat()/bitmap.width,maxH.toFloat()/bitmap.height))
        var w=(bitmap.width*ratio).roundToInt().coerceAtLeast(2) and 1.inv()
        var h=(bitmap.height*ratio).roundToInt().coerceAtLeast(2) and 1.inv()
        return scaleExact(bitmap,w,h)
    }

    private fun scaleExact(bitmap:Bitmap,width:Int,height:Int):Bitmap=
        if(bitmap.width==width&&bitmap.height==height)bitmap
        else Bitmap.createScaledBitmap(bitmap,width,height,true)

    private fun bitmapToYuv420(bitmap:Bitmap,semiPlanar:Boolean):ByteArray{
        val w=bitmap.width;val h=bitmap.height
        val pixels=IntArray(w*h);bitmap.getPixels(pixels,0,w,0,0,w,h)
        val out=ByteArray(w*h*3/2);var yIndex=0
        val uStart=w*h;val vStart=uStart+w*h/4;var uIndex=uStart;var vIndex=vStart;var uvIndex=uStart
        for(j in 0 until h){
            for(i in 0 until w){
                val c=pixels[j*w+i];val r=c shr 16 and 255;val g=c shr 8 and 255;val b=c and 255
                out[yIndex++]=((77*r+150*g+29*b) shr 8).coerceIn(0,255).toByte()
                if(j%2==0&&i%2==0){
                    val u=(((-43*r-85*g+128*b) shr 8)+128).coerceIn(0,255).toByte()
                    val v=(((128*r-107*g-21*b) shr 8)+128).coerceIn(0,255).toByte()
                    if(semiPlanar){out[uvIndex++]=u;out[uvIndex++]=v}else{out[uIndex++]=u;out[vIndex++]=v}
                }
            }
        }
        return out
    }
}
