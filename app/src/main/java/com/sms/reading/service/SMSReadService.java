package com.sms.reading.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.location.Location;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.sms.reading.SMSApplication;
import com.sms.reading.db.DBHelper;
import com.sms.reading.db.SmsTable;
import com.sms.reading.model.ParseSms;
import com.sms.reading.model.ShortSms;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Yogi on 30/07/2016.
 */
public class SMSReadService extends Service {
    private static final String TAG = SMSReadService.class.getSimpleName();
    public static boolean mParseCancelled = false;
    final Context context;
    private final IBinder mBinder;
    private SMSApplication smsApplication;
    private LocalBroadcastManager localBroadcastManager;
    private SharedPreferences sp;
    private DBHelper dbhelper;
    private SmsTable smsTable;
    private WalnutServiceHandler mServiceHandler;

    public SMSReadService() {
        this.context = this;
        mBinder = new LocalBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        smsApplication = SMSApplication.getInstance();
        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        localBroadcastManager = LocalBroadcastManager.getInstance(context);
        dbhelper = smsApplication.getDbHelper();
        smsTable = dbhelper.getSmsTable();
        HandlerThread thread = new HandlerThread("ServiceStartArguments", 10);
        thread.start();
        this.mServiceHandler = new WalnutServiceHandler(thread.getLooper(), this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent;
        if (intent.getAction().equals("walnut.service.NEW_DATA")) {
            msg.what = 1;
            mServiceHandler.sendMessage(msg);
        }
        return 3;
    }

    private long parseAllSmsFromProvider(long lastRead, ShortSms ignoreSms) {
        Uri uri = Uri.parse("content://sms/inbox");
        String[] columns = new String[]{"_id", "body", "address", "date_sent", "date"};
        if (lastRead == -1) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());
            cal.set(Calendar.DATE, 0);
            cal.add(Calendar.MONTH, -2);
            Log.d(TAG, "Fresh read from provider : reading only last 3 months data : " + cal.getTime() + " : " + new Date(System.currentTimeMillis()));
            lastRead = cal.getTimeInMillis();
        }
//        long date1 = new Date(System.currentTimeMillis() - 30L * 24 * 3600 * 1000).getTime();

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
                SMSApplication.broadcastReadSmsPermissionRequest(localBroadcastManager);
            }
        }
        if (c == null) {
            return lastRead;
        }
        int totalCount = c.getCount();
        int count = 0;
        if (c.getCount() > 0) {
            Log.d(TAG, "Parsing " + totalCount + " SMSs");
            smsApplication.setupRules();
        }
        c.moveToFirst();
        long lastReadIgnoreCurrSms = lastRead;
        boolean foundIgnoreSmsStopParsing = false;
        while (!c.isAfterLast() && !mParseCancelled && !foundIgnoreSmsStopParsing) {
            String body = c.getString(c.getColumnIndexOrThrow("body"));
            String number = c.getString(c.getColumnIndexOrThrow("address"));
            if (!dateSentNotPresent) {
                lastRead = c.getLong(c.getColumnIndex("date_sent"));
                Log.d(TAG, "Reading DATE_SENT " + c.getLong(c.getColumnIndex("date_sent")) + " DATE " + c.getLong(c.getColumnIndexOrThrow("date")));
            }
            if (dateSentNotPresent || lastRead <= 0) {
                Log.d(TAG, "Reading DATE Field either DATE_SENT not present or <= 0 lastRead = " + lastRead);
                lastRead = c.getLong(c.getColumnIndexOrThrow("date"));
            }
            long smsId = (long) c.getInt(c.getColumnIndexOrThrow("_id"));
            Date date = new Date(lastRead);
            int nonPersonalSmsCnt = 0;
            if (number == null) {
                Throwable tr = new IllegalAccessException();
                Log.d(TAG, "Sms from: " + number + " body: " + body, tr);
            } else if (body != null) {
                Log.d(TAG, "Processing *********** " + number);
                if (ignoreSms == null || !TextUtils.equals(body.trim(), ignoreSms.getBody().trim())) {
                    lastReadIgnoreCurrSms = lastRead;
                    if (smsTable.isDuplicate(number, body, date)) {
                        Log.d(TAG, "Duplicate SMS: " + number + " / " + body + " / " + date);
                    } else {
                        try {
                            parseAndStoreToDB(number, body, date, smsId, null, false);

                        } catch (Throwable e3) {
                            Log.e(TAG, "*** Exception while Parsing SMS from provider: " + date + " : " + number + " " + body, e3);
                        }
                    }
                } else {
                    Log.d(TAG, "Ignoring current SMS time from broadcast [" + ignoreSms.getDate().getTime() + "] from db [" + date.getTime() + "]");
                    foundIgnoreSmsStopParsing = true;
                }
                if (ignoreSms != null && nonPersonalSmsCnt > 0) {
                    Log.d(TAG, "Logging Missing Sms Exception For Sms from: [" + number + "] \nbody: [" + body + "] \ndate [" + date.toGMTString() + "] Time : [" + date.getTime() + "] \n\nbroadcast triggering SMS from [" + ignoreSms.getNumber() + "] \nbody [" + ignoreSms.getBody() + "] \ndate [" + ignoreSms.getDate().toGMTString() + "] TIME [ " + ignoreSms.getDate().getTime());
                }
            }
            c.moveToNext();
            count++;
            if (!foundIgnoreSmsStopParsing) {
                SMSApplication.broadcastProgress(localBroadcastManager, count + "/" + totalCount);
            }
        }
        c.close();
        if (lastRead != 0 || totalCount == 0) {
            return lastReadIgnoreCurrSms;
        }
        SMSApplication.broadcastToast(localBroadcastManager, "SMS Parser read " + totalCount + " records and  found " + count + " transactional SMSs");
        return lastReadIgnoreCurrSms;
    }

    private ArrayList<ShortSms> parseAndStoreToDB(String number, String body, Date date, long smsId, Location loc, boolean gaHit) {
        ArrayList<ShortSms> smsList = ParseSms.Parse(this, number, body, date);
//        Log.d(TAG, "" + smsList.toString());
        return smsList;
    }


    private class WalnutServiceHandler extends Handler {
        private final String TAG;
        private SMSReadService service;

        public WalnutServiceHandler(Looper looper, SMSReadService service) {
            super(looper);
            this.TAG = WalnutServiceHandler.class.getSimpleName();
            this.service = service;
        }

        @Override
        public void handleMessage(Message msg) {
            handleReadDataAction(msg.arg1, (Intent) msg.obj);
        }

        private void handleReadDataAction(int startId, Intent intent) {
            int mode = intent.getExtras().getInt("walnut.service.mode", 0);
            mParseCancelled = false;
            if (mode == 1) {
                long startTime = intent.getExtras().getLong("walnut.service.timestamp", -1);
                Log.d(TAG, "Read mode  = READ_MODE_FROM_SMS " + startTime);
                String str = "Pref-LastReadSMS";
                sp.edit().putLong(str, parseAllSmsFromProvider(startTime, null)).apply();
            }
            if (!mParseCancelled) {
                SMSApplication.broadcastFinish(localBroadcastManager);
            }
            stopForeground(true);
            Log.d(TAG, "All done");
            service.stopSelf(startId);
        }
    }

    public class LocalBinder extends Binder {
    }

}
