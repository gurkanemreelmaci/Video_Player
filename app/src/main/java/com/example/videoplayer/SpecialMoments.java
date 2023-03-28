package com.example.videoplayer;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Özel anların ArrayList üzerine kayıt edilmesini ve
 * listView içerisine aktarılmasını sağlar
 */

public class SpecialMoments extends AppCompatActivity {

    private ArrayList<String> specialMomentsTime = new ArrayList<>();
    private ArrayList<String> specialMomentsList = new ArrayList<>();
    private ListView listView;
    private File path ;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spacial_moments);
        path = getApplicationContext().getFilesDir();
        listView = findViewById(R.id.special_moment_list_view);
        Bundle gelenVeri = getIntent().getExtras();
        specialMomentsTime = gelenVeri.getStringArrayList("timesArray");

        // specialMomentsList üzerinde arayüz düzenlemeleri yapılacak
        MiliSecondToMinute();
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, specialMomentsList);
        try {
            listView.setAdapter(arrayAdapter);
        }catch (Exception e){
            Log.e("Null_Pointer_Exception",e.getMessage());
        }
    }
    //özel analrın txt dosyasına kayıt edilmesini sağlar
    public void saveFile(){
        try {
            FileOutputStream outputStream = new FileOutputStream(new File(path,"Video.txt"));
            outputStream.write(specialMomentsTime.toString().getBytes());
            outputStream.close();
        } catch (IOException e) {
            Log.e("IO_Exception",e.getClass().toString());
        }
    }
    // video üzerinden gelen mili saniye değerini dakika yapısına dönüştürür
    public void MiliSecondToMinute(){
        for (int i = 0; i<specialMomentsTime.size(); i++){
            int temp = Integer.parseInt(specialMomentsTime.get(i));
            int second = temp/1000;
            int minute = second/60;
            second = second-60*minute;
            specialMomentsList.add(minute + ":" + second);
        }
    }

    @Override
    protected void onDestroy() {
        saveFile();
        super.onDestroy();
    }
    // Ana uygulamaya geri dönülmesi anaınada videonun kaldığı
    // yerden devam etmesi için gerekli değerleri ana ugulamaya gönderir
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this,MainActivity.class);
        intent.putExtra("timesArray",specialMomentsTime);
        intent.putExtra("currentTime",Integer.parseInt(specialMomentsTime.get(specialMomentsTime.size()-1)));
        startActivity(intent);
        super.onBackPressed();
    }
}