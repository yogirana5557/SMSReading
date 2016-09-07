package com.sms.reading;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sms.reading.model.ShortSms;
import com.sms.reading.service.SMSReadService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_READ_SMS = 2;
    private static final String TAG = MainActivity.class.getSimpleName();
    private final BroadcastReceiver mWalnutReceiver;
    ArrayList<ShortSms> smsList;
    private LocalBroadcastManager localBroadcastManager;
    private ProgressBar statusTopProgressBar;
    private TextView statusTopText;
    private List<String> mProviderList;

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
//        startService();
//        localBroadcastManager = LocalBroadcastManager.getInstance(this);
//        localBroadcastManager.registerReceiver(mWalnutReceiver, makeWalnutUpdatesIntentFilter());
        mProviderList = new ArrayList<>();
        mProviderList.add("Amazon");
        mProviderList.add("IPAYTM");

        readSms();
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

    private void readSms() {
        long lastRead;
        Uri uri = Uri.parse("content://sms/inbox");
        String[] columns = new String[]{"_id", "body", "address", "date_sent", "date"};
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.set(Calendar.DATE, 0);
        cal.add(Calendar.MONTH, -6);
        Log.d(TAG, "Fresh read from provider : reading only last 3 months data : " + cal.getTime() + " : " + new Date(System.currentTimeMillis()));
//        long date1 = new Date(System.currentTimeMillis() - 30L * 24 * 3600 * 1000).getTime();

        lastRead = cal.getTimeInMillis();
        String[] selectionArgs = new String[]{String.valueOf(lastRead)};
        Cursor c = null;
        boolean dateSentNotPresent = false;

        try {
            c = getContentResolver().query(uri, columns, "date_sent > ? ", selectionArgs, "date_sent ASC");
        } catch (SQLiteException | SecurityException e) {
            try {
                c = getContentResolver().query(uri, new String[]{"_id", "body", "address", "date"}, "date > ? ", selectionArgs, "date ASC");
                dateSentNotPresent = true;

            } catch (SecurityException e2) {
                e.printStackTrace();
            }
        }
        if (c == null) {
            return;
        }
        int totalCount = c.getCount();
        if (c.getCount() > 0) {
            Log.d(TAG, "Parsing " + totalCount + " SMSs");
        }
        c.moveToFirst();
        smsList = new ArrayList<>();
        while (!c.isAfterLast()) {
            String body = c.getString(c.getColumnIndexOrThrow("body"));
            String number = c.getString(c.getColumnIndexOrThrow("address"));
            long smsId = (long) c.getInt(c.getColumnIndexOrThrow("_id"));
            if (!dateSentNotPresent) {
                lastRead = c.getLong(c.getColumnIndex("date_sent"));
                Log.d(TAG, "Reading DATE_SENT " + c.getLong(c.getColumnIndex("date_sent")) + " DATE " + c.getLong(c.getColumnIndexOrThrow("date")));
            }
            if (dateSentNotPresent || lastRead <= 0) {
                lastRead = c.getLong(c.getColumnIndexOrThrow("date"));
            }
            Date date = new Date(lastRead);

            if (number == null) {
                Throwable tr = new IllegalAccessException();
                Log.d(TAG, "Sms from: " + number + " body: " + body, tr);
            } else if (body != null) {
                ShortSms shortSms = parseAndStoreToDB(number, body, date, smsId);
                if (shortSms != null) {
                    smsList.add(shortSms);
                    Log.d(TAG, "" + shortSms.toString());
                }
            }
            c.moveToNext();
        }
        c.close();
    }

    private ShortSms parseAndStoreToDB(String number, String body, Date date, long smsId) {
        String[] names = number.split("-");
        boolean matcher = false;
        if (names.length == 2) {
            matcher = mProviderList.contains(names[1]);
        } else if (names.length != 1) {
            matcher = mProviderList.contains(number);

        } else if (number.matches("(?i)[0-9]{1,7}\\s*")) {
            matcher = mProviderList.contains(names[0]);
        }
        if (matcher) {
            ShortSms shortSms = new ShortSms(number, body, date);
            Log.d(TAG, "Number: " + number);
            return shortSms;
        }
        return null;
    }
}
