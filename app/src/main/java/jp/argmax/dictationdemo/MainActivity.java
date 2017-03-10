package jp.argmax.dictationdemo;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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


public class MainActivity extends AppCompatActivity {

    @State
    String mButtonText = "start";
    @State
    Boolean nowRecording = false;
    @State
    ArrayList<String> mASRResultList = new ArrayList<>();
    private ArrayAdapter<String> mArrayAdapter;

    private IntentFilter intentFilter;
    private BroadcastReceiver recv;

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

        intentFilter = new IntentFilter();
        intentFilter.addAction("SEND_ASR_RESULT");
        recv = new ASRResultReceiver();
        registerReceiver(recv, intentFilter);
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
                stopService(new Intent(MainActivity.this, MicRecordingASRService.class));
            }else{
                // 開始
                mButtonText = "stop";
                mArrayAdapter.insert("start", 0);
                startService(new Intent(MainActivity.this, MicRecordingASRService.class));

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
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    addClickListener();
                }
        }
    }


    private class ASRResultReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            String message = bundle.getString("ASR_RESULT");
            mArrayAdapter.insert(message, 0);
        }
    }

}
