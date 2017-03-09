package jp.argmax.dictationdemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

import icepick.Icepick;
import icepick.State;
import jp.argmax.dictationdemo.recorder.MicRecordingService;


public class MainActivity extends AppCompatActivity {

    @State
    String mButtonText = "start";
    @State
    Boolean nowRecording = false;
    @State
    ArrayList<String> mASRResultList = new ArrayList<>();
    private ArrayAdapter<String> mArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Icepick.restoreInstanceState(this, savedInstanceState);
        mArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mASRResultList);
        ListView lv = (ListView) findViewById(R.id.asr_listtview);
        lv.setAdapter(mArrayAdapter);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            int REQUEST_CODE = 1;
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.RECORD_AUDIO
            }, REQUEST_CODE);

        } else {
            addClickListener();
        }

    }

    private void addClickListener(){
        //buttonを取得
        Button btn = (Button)findViewById(R.id.asr_button);
        btn.setText(mButtonText);
        btn.setOnClickListener(view -> {
            if(nowRecording) {
                // 停止
                mButtonText = "start";
                mArrayAdapter.insert("stop", 0);
                stopService(new Intent(MainActivity.this, MicRecordingService.class));
            }else{
                // 開始
                mButtonText = "stop";
                mArrayAdapter.insert("start", 0);
                startService(new Intent(MainActivity.this, MicRecordingService.class));

            }
            ((Button)view).setText(mButtonText);
            //mArrayAdapter.notifyDataSetChanged();
            nowRecording = !nowRecording;

        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                addClickListener();
        }
    }


}
