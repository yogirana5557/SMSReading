package com.sms.reading;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sms.reading.service.SMSReadService;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_READ_SMS = 2;
    private static final String TAG = MainActivity.class.getSimpleName();
    private final BroadcastReceiver mWalnutReceiver;
    private LocalBroadcastManager localBroadcastManager;
    private ProgressBar statusTopProgressBar;
    private TextView statusTopText;

    public MainActivity() {
        mWalnutReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("walnut.app.WALNUT_PROGRESS".equals(intent.getAction())) {
                    String progressString = intent.getStringExtra("walnut.app.WALNUT_PROGRESS_EXTRA_STRING");
                    String progressStringPrefix = intent.getStringExtra("walnut.app.WALNUT_PROGRESS_EXTRA_STRING_PREFIX");
                    if (progressStringPrefix != null) {
                        statusTopText.setText(progressStringPrefix + " " + progressString);
                    } else {
                        statusTopText.setText("SMS Reading:" + " " + progressString);
                    }
                    String[] values = progressString.split("/");
                    if (values.length == 2) {
                        if (statusTopProgressBar.getMax() != Integer.valueOf(values[1]).intValue()) {
                            statusTopProgressBar.setMax(Integer.valueOf(values[1]).intValue());
                        }
                        statusTopProgressBar.setProgress(Integer.valueOf(values[0]).intValue());
                    }
                } else if ("walnut.app.WALNUT_FINISH".equals(intent.getAction())) {
                    Log.d(TAG, "Done");
                } else if ("walnut.app.REQUEST_FOR_READ_SMS_PERM".equals(intent.getAction())) {
                    showPermissionDialog();
                }

            }
        };
    }

    public static IntentFilter makeWalnutUpdatesIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("walnut.app.WALNUT_PROGRESS");
        intentFilter.addAction("walnut.app.WALNUT_FINISH");
        intentFilter.addAction("walnut.app.REQUEST_FOR_READ_SMS_PERM");
        return intentFilter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        statusTopProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        statusTopText = (TextView) findViewById(R.id.progressText);
        startService();
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(mWalnutReceiver, makeWalnutUpdatesIntentFilter());
    }

    public void onDestroy() {
        Log.d(TAG, "---------onDestroy-------");
        super.onDestroy();
        if (localBroadcastManager != null) {
            localBroadcastManager.unregisterReceiver(mWalnutReceiver);
        }
    }

    private void showPermissionDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.layout_sms_permission_dialog);
        dialog.show();
        AppCompatButton enableBtn = (AppCompatButton) dialog.findViewById(R.id.enable);
        enableBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_SMS},
                        REQUEST_READ_SMS);
            }
        });

        AppCompatButton exitBtn = (AppCompatButton) dialog.findViewById(R.id.exit);
        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                finish();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == REQUEST_READ_SMS) {
            // Check if the only required permission has been granted
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startService();
            } else {
                showPermissionDialog();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void startService() {
        Log.d(TAG, "Service started");
        Intent walnutServiceIntent = new Intent(this, SMSReadService.class);
        walnutServiceIntent.setAction("walnut.service.NEW_DATA");
        walnutServiceIntent.putExtra("walnut.service.timestamp", (long) -1);
        walnutServiceIntent.putExtra("walnut.service.mode", 1);
        startService(walnutServiceIntent);
    }

}
