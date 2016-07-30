package com.sms.reading;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.sms.reading.service.SMSReadService;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent walnutServiceIntent = new Intent(this, SMSReadService.class);
        walnutServiceIntent.setAction("walnut.service.NEW_DATA");
        walnutServiceIntent.putExtra("walnut.service.mode", 1);
        startService(walnutServiceIntent);
    }
}
