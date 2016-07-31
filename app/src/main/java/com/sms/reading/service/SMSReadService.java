package com.sms.reading.service;

import android.app.Service;
import android.content.ContentValues;
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
import com.sms.reading.db.AccountTable;
import com.sms.reading.db.DBHelper;
import com.sms.reading.db.SmsTable;
import com.sms.reading.db.TransactionTable;
import com.sms.reading.model.Account;
import com.sms.reading.model.AccountBalance;
import com.sms.reading.model.ChainingRule;
import com.sms.reading.model.ParseSms;
import com.sms.reading.model.ShortSms;
import com.sms.reading.model.Transaction;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

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
    private AccountTable accountTable;
    private TransactionTable transactionTable;
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
        accountTable = dbhelper.getAccountTable();
        transactionTable = dbhelper.getTransactionTable();
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
        boolean notFirstParse = lastRead > 0;
        Uri uri = Uri.parse("content://sms/inbox");
        String[] columns = new String[]{"_id", "body", "address", "date_sent", "date"};
        if (lastRead == 0) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());
            cal.set(Calendar.DATE, 0);
            cal.add(Calendar.MONTH, -2);
            Log.d(TAG, "Fresh read from provider : reading only last 3 months data : " + cal.getTime() + " : " + new Date(System.currentTimeMillis()));
            lastRead = cal.getTimeInMillis();
        }
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
                            ArrayList<ShortSms> smsList = parseAndStoreToDB(number, body, date, smsId, null, false);
                            if (!(smsList == null || smsList.isEmpty())) {
                                Iterator it = smsList.iterator();
                                while (it.hasNext()) {
                                    ShortSms sms = (ShortSms) it.next();
                                    if ((sms instanceof Transaction) && notFirstParse) {
                                        ((Transaction) sms).findSimilarTxnAndUpdate(context, dbhelper);
                                        nonPersonalSmsCnt++;
                                    }
                                }
                            }
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
        ArrayList<ShortSms> newSmsList = new ArrayList<>();
        if (!(smsList == null || smsList.isEmpty())) {
            Iterator it = smsList.iterator();
            while (it.hasNext()) {
                ShortSms sms = (ShortSms) it.next();
                if (sms != null) {
                    if (sms.getCategory() != null) {
                        sms.setLocation(loc);
                        sms.setSmsId(smsId);
                        storeToDB(sms, null, gaHit);
                        newSmsList.add(sms);
                    }
                }
            }
        }
        return newSmsList;
    }

    private void storeToDB(ShortSms newSms, ShortSms oldSms, boolean gaHit) {
        if (newSms.isParsed() || oldSms == null) {
            Account acc;
            if (newSms.getAccountType() == 99) {
                acc = accountTable.createAccount(newSms.getCategory(), "Messages", 99);
            } else {
                acc = accountTable.createAccount(newSms.getCategory(), "Messages", 9);
            }
            ContentValues values = new ContentValues();
            if (oldSms == null) {
                newSms.set_id(smsTable.writeSmsToDb(acc, newSms, false));
                oldSms = newSms;
            } else {
                values.put("accountId", acc.get_id());
            }
            long smsId = oldSms.get_id();
            ArrayList<ChainingRule.MatchingCriteria> matchingCriteriaArrayList;
            if (newSms instanceof Transaction) {
                Transaction txn = (Transaction) newSms;
                txn.setSmsId(oldSms.get_id());
                if (txn.isChainingEnabled()) {
                    matchingCriteriaArrayList = txn.getParentSelectionCriteriaList();
                    ShortSms oldTxn = this.transactionTable.getLastMatchingTransaction(this.accountTable.getAccountsByName(txn.getCategory()), matchingCriteriaArrayList, txn);
                    if (oldTxn != null) {
//                        updateTransaction(txn, oldTxn);
                        //  cancelNotification(oldTxn, (int) oldTxn.get_id(), newSms);
                    } else if (txn.shouldDeleteChild()) {
                        txn.setFlags(txn.getFlags() | 16);
                    } else {
                        txn.setFlags(txn.getFlags() & -17);
                    }
                }
                if (txn.getTxnType() == 12 || txn.getTxnType() == 17) {
                    acc = this.accountTable.createAccount(txn.getCategory() + " " + Account.getAccountNamePostfix(txn.getAccountType()), txn.getPanNo(), txn.getAccountType(), txn.isExpenseAccount(), txn.getAccountOverrideName());
                } else {
                    acc = this.accountTable.createAccount(txn.getCategory() + " " + Transaction.getAccountNamePostfix(txn.getTxnType()), txn.getPanNo(), txn.getAccountType(), txn.isExpenseAccount(), txn.getAccountOverrideName());
                }
                txn.setAccountId(acc.get_id());
                txn.setIsAccountEnabled(acc.isEnabled());
                txn.setDisplayPan(acc.getDisplayPan());
                txn.setAccountDisplayName(acc.getDisplayName());
                if (!acc.isEnabled()) {
                    txn.setFlags(txn.getFlags() | 8);
                }
                Account parentAccount;
                ContentValues accValues;
                int accFlag;
                if (txn.getBalance() != null) {
                    parentAccount = this.dbhelper.getParentAccount((long) acc.get_id());
                    if (parentAccount != null) {
                        acc = parentAccount;
                    }
                    boolean isLatestBalDate = true;
                    boolean isLatestOutBalDate = true;
                    if (acc.getBalanceInfo() != null) {
                        isLatestBalDate = AccountBalance.isLatest(txn.getDate(), acc.getBalanceInfo().getBalSyncDate());
                        isLatestOutBalDate = AccountBalance.isLatest(txn.getDate(), acc.getBalanceInfo().getOutbalSyncdate());
                    }
                    AccountBalance accBal = new AccountBalance();
                    accValues = new ContentValues();
                    if (isLatestBalDate) {
                        accBal.setBalance(txn.getBalance().getBalance());
                        accBal.setBalSyncDate(txn.getDate());
                    } else {
                        accBal.setBalance(Double.MIN_VALUE);
                    }
                    if (isLatestOutBalDate) {
                        accBal.setOutstandingBalance(txn.getBalance().getOutstandingBalance());
                        accBal.setOutbalSyncdate(txn.getDate());
                    } else {
                        accBal.setOutstandingBalance(Double.MIN_VALUE);
                    }
                    acc.setBalanceInfo(accBal);
                    AccountTable.putBalance(accValues, acc.getBalanceInfo());
                    if (accValues.size() > 0) {
                        accValues.put("updatedTime", Long.valueOf(System.currentTimeMillis()));
                        accFlag = acc.getFlags();
                        if (accValues.containsKey("balance")) {
                            accFlag &= -5;
                        } else {
                            accFlag |= 4;
                        }
                        if (accValues.containsKey("outstandingBalance")) {
                            accFlag &= -9;
                        } else {
                            accFlag |= 8;
                        }
                        acc.setFlags(accFlag);
                        accValues.put("flags", Integer.valueOf(accFlag));
                        dbhelper.updateAccount(acc, accValues);
                    } else {
                        txn.setBalance(null);
                    }
                } else {
                    parentAccount = this.dbhelper.getParentAccount((long) acc.get_id());
                    if (parentAccount != null) {
                        acc = parentAccount;
                    }
                    accFlag = acc.getFlags();
                    boolean isTxnDateLatestThanBal = true;
                    boolean isTxnDateLatestThanOutbal = true;
                    if (acc.getBalanceInfo() != null) {
                        isTxnDateLatestThanBal = AccountBalance.isLatest(txn.getTxnDate(), acc.getBalanceInfo().getBalSyncDate());
                        isTxnDateLatestThanOutbal = AccountBalance.isLatest(txn.getTxnDate(), acc.getBalanceInfo().getOutbalSyncdate());
                    }
                    if (isTxnDateLatestThanBal) {
                        accFlag |= 4;
                    }
                    if (isTxnDateLatestThanOutbal) {
                        accFlag |= 8;
                    }
                    acc.setFlags(accFlag);
                    accValues = new ContentValues();
                    accValues.put("updatedTime", Long.valueOf(System.currentTimeMillis()));
                    accValues.put("flags", Integer.valueOf(accFlag));
                    dbhelper.updateAccount(acc, accValues);
                }
                if (txn.getTxnType() == 12 || txn.getTxnType() == 17) {
                    txn.setFlags(16);
                }
                String category = txn.getTxnCategories();
                if (!TextUtils.isEmpty(category)) {
                    if (Transaction.isNotExpenseTxnTypes(txn.getTxnType()) && category.equals("")) {
                        txn.setIsNotAnExpense();
                    }
                    if (Transaction.isBillTxnTypes(txn.getTxnType()) && category.equals("")) {
                        txn.setTxnCategories("");
                    }
                }
                txn.set_id(transactionTable.writeTransactionToDb(txn));
                values.put("parsed", Boolean.valueOf(true));
                this.smsTable.updateMessage(smsId, values);
            }
        }
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
