package com.example.videoplayer;
/**
 * Created by Gürkan Emre Elmacı
 */


/**
 * Read Me
 * Oluşturulan bu yapı video üzerinde özel anların kaydedilmesini,
 * video hızının ayarlanmasını, ekran kayıt işlemi kontrolünü ve
 * video kontrol işlemlerini yapmaktadır.
 *
 */

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity{

    private VideoView videoView;
    private Button replaySpeedForwardBtn;
    private float videoSpeed = 1f;
    private Button specialMomentBtn;
    private Button backSpecialMomentBtn;
    private Button nextSpecialMomentBtn;
    private Button startServiceBtn;
    private Button stopServiceBtn;

    private Intent launchIntent;
    private static final int REQUEST_PERMISSION = 1;
    private int currentSpacialMoment = 0;
    private ArrayList<String> specialMomentsTime = new ArrayList<>();
    private File path;
    private int currentPosition = 0;
    private final String[] permissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint({"SetTextI18n", "ResourceAsColor"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        path = getApplicationContext().getFilesDir();
        Bundle gelenVeri = getIntent().getExtras();

        // Özel anlar için oluşturulan yapı üzerinden geri dönüş olarak eğer veri geliyor ise verilerin alınması sağlanır

        if(gelenVeri != null){
            currentPosition = gelenVeri.getInt("currentTime");
            specialMomentsTime = gelenVeri.getStringArrayList("timesArray");
            if(specialMomentsTime == null) specialMomentsTime = new ArrayList<>();

        }else{
            loadList();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getPermission();
        }

        //arayüz elemanlarına erşim sağlanıyor
        replaySpeedForwardBtn = findViewById(R.id.speed_btn);
        videoView = findViewById(R.id.videoView);
        specialMomentBtn = findViewById(R.id.special_moment_button);
        nextSpecialMomentBtn = findViewById(R.id.next_special_moment);
        backSpecialMomentBtn = findViewById(R.id.back_special_moment);
        startServiceBtn = findViewById(R.id.open_app_btn);
        stopServiceBtn = findViewById(R.id.stop_btn);

        //başlangıç işlemleri oluşturuluyor
        path = getApplicationContext().getFilesDir();
        replaySpeedForwardBtn.setText("1X");
        replaySpeedForwardBtn.setTextColor(R.color.black);

        replaySpeedForwardBtn.setOnClickListener(speedChangeVideoListener);
        specialMomentBtn.setOnClickListener(setSpecilaMomentListener);
        backSpecialMomentBtn.setOnClickListener(backSpecialMomentListener);
        nextSpecialMomentBtn.setOnClickListener(nextSpecialMomentListener);
        startServiceBtn.setOnClickListener(startServiceListener);
        stopServiceBtn.setOnClickListener(stopServiceListener);

        // video başlatılıyor
        playVideo();
        launchIntent = new Intent(this,RecordService.class);
        // öncesinde oluşturulmuş veriler dosya üzerinden alınır
    }

        // Arka planda çalışan servisin kapatılmasını sağlar
    public void stopService(){
        stopService(launchIntent);
    }
    @Override
    protected void onStop() {
        saveFile();
        //stopService();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        //uygulama kapatılma anında oluşturulan veriler kayıt edilir
        saveFile();
        stopService();
        super.onDestroy();
    }

    // uygulama çalışma aşamasında oluşturulan özel anlar txt dosyasına kaydedilir
    public void saveFile(){
        try {
            FileOutputStream outputStream = new FileOutputStream(new File(path,"Video.txt"));
            outputStream.write(specialMomentsTime.toString().getBytes());
            outputStream.close();
        } catch (IOException e) {
            Log.e("IO_Exception",e.getClass().toString());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void loadList(){

        // uygulamam verilerinin tutulduğu dosyaya erişim sağlanır
        File readFrom = new File(path,"Video.txt");
        byte[] content = new byte[(int) readFrom.length()];
        try {
            FileInputStream fileInputStream = new FileInputStream(readFrom);
            fileInputStream.read(content);
            String readValue = new String(content);

            readValue = readValue.substring(1,readValue.length() -1);
            String[] List = readValue.split(", ");
            specialMomentsTime = new ArrayList<>(Arrays.asList(List));
        }
        catch (IOException | StringIndexOutOfBoundsException e) {
            Log.e("IO_Exception",e.getClass().toString());
        }
    }

    private void playVideo() {
        try {
            // uygulama açılış aşamasında video dosyasına erişilir ve video başlatılır
            String selectedVideoPath = "android.resource://" + getPackageName() + "/raw/video";// video path belli olmadıpı için path raw dosyası olarak belirlenmiştir.
            videoView.setVideoPath(selectedVideoPath);

            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(videoView);

            videoView.setMediaController(mediaController);
            videoView.start();
            if(currentPosition !=0){
                videoView.seekTo(currentPosition);
            }
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    // video hızının ayarlandığı metottur bu yapıda seçilen hıza göre video yeniden düzenleniyor
    @SuppressLint("SetTextI18n")
    private void speedChange(){
        CharSequence text = replaySpeedForwardBtn.getText();

        if ("1X".contentEquals(text)) {
            replaySpeedForwardBtn.setText("2X");
            videoSpeed = 2f;
        } else if ("2X".contentEquals(text)) {
            replaySpeedForwardBtn.setText("3X");
            videoSpeed = 3f;
        } else if ("3X".contentEquals(text)) {
            replaySpeedForwardBtn.setText("4X");
            videoSpeed = 4f;
        } else if ("4X".contentEquals(text)) {
            replaySpeedForwardBtn.setText("5X");
            videoSpeed = 5f;
        } else if ("5X".contentEquals(text)) {
            replaySpeedForwardBtn.setText("6X");
            videoSpeed = 6f;
        } else if ("6X".contentEquals(text)) {
            replaySpeedForwardBtn.setText("7X");
            videoSpeed = 7f;
        } else if ("7X".contentEquals(text)) {
            replaySpeedForwardBtn.setText("0.5X");
            videoSpeed = 0.5f;
        } else if ("0.5X".contentEquals(text)) {
            replaySpeedForwardBtn.setText("1X");
            videoSpeed = 1f;
        }
        int temp = videoView.getCurrentPosition();
        videoView.setOnPreparedListener(speedChangeMediaPlayerListener);
        videoView.stopPlayback();
        playVideo();
        videoView.seekTo(temp);

    }


    // video uzerinde önemli noktaların kaydedilmesini
    // ve ardından kaydedilen verileri bulunduğu listeyi gösterir
    private void setSpecialMoment(){
        // specialMoment butonuna basılması anında işlemleri gerçekleyecek aktiviteye veri gönderilmesi
        // ve başlatılmasını sağlar
        try {
            specialMomentsTime.add(Integer.toString(videoView.getCurrentPosition()));
        }catch (Exception e){
            Log.e("Run_Time",e.getClass().toString());
        }

        Intent intent = new Intent(this,SpecialMoments.class);
        intent.putExtra("timesArray",specialMomentsTime);
        startActivity(intent);
    }

    private void nextSpecialMoment(){
        // özel anlar üzerinde ileri yönde gezinmeyi sağlar
        if(specialMomentsTime.size() != 0){
            videoView.seekTo(Integer.parseInt(specialMomentsTime.get(currentSpacialMoment)));
            if(currentSpacialMoment != (specialMomentsTime.size()-1))
            {
                currentSpacialMoment++;
            }
        }
    }

    private void backSpecialMoment(){
        // özel anlar üzerinde geri yönde gezinmeyi sağlar
        if(specialMomentsTime.size() != 0){
            videoView.seekTo(Integer.parseInt(specialMomentsTime.get(currentSpacialMoment)));
            if(currentSpacialMoment != 0)
            {
                currentSpacialMoment--;
            }
        }
    }
    // Arka planda çalışacak olan video kayıt servisinin başlatılmasını sağlar
    @SuppressLint("SetTextI18n")
    private void startService(){
            startService(launchIntent);
    }


    // başlangıç aşamasında uygulamanın çalışması için gerekli olan izinlere
    // sahip olup olmadığı kontrol ediliyor
    @SuppressLint("ObsoleteSdkInt")
    @RequiresApi(api = Build.VERSION_CODES.P)
    private void getPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{
                                Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.FOREGROUND_SERVICE},11);
            }
        }
    }

    // uyugulama izinlerinin kontrol edilmesi aşamasında eğer izinler yok ise izinler alınıyor
    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 11){
            if (grantResults.length > 0){
                if(grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    this.getPermission();
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    // uygulama içerinde kullanılacak olan listener yapıları oluşturuluyor
    @SuppressLint("SetTextI18n")
    private final MediaPlayer.OnPreparedListener speedChangeMediaPlayerListener = mp -> mp.setPlaybackParams(mp.getPlaybackParams().setSpeed(videoSpeed));

    private final View.OnClickListener stopServiceListener = v-> stopService();
    private final View.OnClickListener startServiceListener = v -> startService();
    private final View.OnClickListener speedChangeVideoListener = v -> speedChange();
    private final View.OnClickListener setSpecilaMomentListener = v -> setSpecialMoment();
    private final View.OnClickListener nextSpecialMomentListener = v -> nextSpecialMoment();
    private final View.OnClickListener backSpecialMomentListener = v -> backSpecialMoment();
}