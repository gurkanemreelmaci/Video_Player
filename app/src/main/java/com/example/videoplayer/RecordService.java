package com.example.videoplayer;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Environment;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.widget.Toast;


import androidx.annotation.RequiresApi;

import java.io.File;

/**
 * created by Gürkan Emre Elmacı
 *
 * Ekran kaydı aşamsında arkaplanda ekrankaydını yapacak olan servis
 *
 */
public class RecordService extends Service{

    // Projede kullanılacak değişkenler tanımlanıyor
    private MediaProjectionManager mediaProjectionManager;
    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;
    private MediaProjectionCallBack mediaProjectionCallBack;

    private int mScreenDensity;
    // metric değişkeni ekran üzerinden genişliği ve yüksekliği gibi özellikleri alınması için kullanılacak
    private DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();


    private MediaRecorder mediaRecorder;
    private boolean isChecked = false;
    private String videoUri = "";
    private String recordingFile = "ScreenRec"+System.currentTimeMillis()+".mp4";
    private Intent pData;

    private final String[] permissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
    };

    public RecordService() {

    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // başlangıç anında servis çalıştırıldığında video kaydı aşamasında kullanılacak değişkenler oluşturuluyor
        Toast.makeText(this, "Service started by user.", Toast.LENGTH_LONG).show();
        pData = intent.getParcelableExtra("resultIntent");
        mScreenDensity = metrics.densityDpi;
        mediaRecorder = new MediaRecorder();
        mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        HandlerThread handlerThread = new HandlerThread("handler thread");
        handlerThread.start();

        // eğer video ile ilğili başlangıç işlemleri yapılmış ise başlangıç aşamalarını atlıyor
        if(!isChecked){
            initRecorder();
            recordScreen();
            isChecked = true;
        }else{
            try{
                mediaRecorder.stop();
                mediaRecorder.reset();
                stopRecordingScreen();
            }catch (Exception e){
                Log.e("deneme",e.getClass().toString());
            }
            isChecked = false;

        }
        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRecordingScreen();
        Toast.makeText(this, "Service destroyed by user.", Toast.LENGTH_LONG).show();
    }


    // video kaydı durdurulması yani servisin sonlandırılması halinde video kaydını sonlanrıyor
    private void stopRecordingScreen() {
        if(virtualDisplay == null){
            return;
        }
        virtualDisplay.release();
        destroyMediaProjection();
    }

    private void destroyMediaProjection() {
        if(mediaProjection == null){
            mediaProjection.stop();
            mediaProjection.unregisterCallback(mediaProjectionCallBack);
            mediaProjection = null;
        }
    }

    // ekran kaydı aşamasında kayıt edilecek ekran virtualDisplay ile belirleniyor
    // ve eğer herhangi bir hata olmaz ise video kaydı başlatılıyor
    private void recordScreen() {
        if(mediaProjection == null){
            startActivity(mediaProjectionManager.createScreenCaptureIntent());
            mediaProjection = mediaProjectionManager.getMediaProjection(-1, pData);
        }
        try{
            Surface surface = mediaRecorder.getSurface();
            virtualDisplay = mediaProjection.createVirtualDisplay("Virtual Device",metrics.widthPixels,metrics.heightPixels,
                    metrics.densityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    surface,null,null);
        }catch (Exception e){
            Log.e("deneme",e.getMessage());
        }


        try{
            mediaRecorder.start();
        }catch (Exception e){
            Log.e("Record Exception",e.getMessage());
        }
    }

    //kaydedilen video özellikleri belirleniyor ve mediaRecorder değişkeni oluşturuluyor
    @RequiresApi(api = Build.VERSION_CODES.S)
    private void initRecorder() {
        try {

            //mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

            File path = Environment.getExternalStorageDirectory();
            File folder = new File(path,"MyScreenRec/");
            if (!folder.exists()){
                folder.mkdir();
            }


            File file1 = new File(folder,recordingFile);
            videoUri = file1.getAbsolutePath();

            mediaRecorder.setOutputFile(videoUri);
            mediaRecorder.setVideoSize(metrics.widthPixels,metrics.heightPixels);
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            //mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setVideoEncodingBitRate(6 * (int) Math.pow(2, 20) *8);
            mediaRecorder.setVideoFrameRate(30);
            mediaRecorder.prepare();
        }catch (Exception e){
            Log.e("Init Exception",e.getMessage());
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    private class MediaProjectionCallBack extends MediaProjection.Callback {
        MediaRecorder mediaRecorder;
        MediaProjection mediaProjection;

        @Override
        public void onStop() {
            if(isChecked){
                isChecked = false;
                mediaRecorder.stop();
                mediaRecorder.reset();
            }

            mediaProjection = null;
            stopRecordingScreen();
            Toast.makeText(RecordService.this, "Recording Stopped", Toast.LENGTH_SHORT).show();
            super.onStop();
        }
    }

}