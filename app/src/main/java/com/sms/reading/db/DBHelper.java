package com.sms.reading.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sms.reading.model.Account;
import com.sms.reading.model.ShortSms;

import java.util.ArrayList;

/**
 * Created by Yogi on 30/07/2016.
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final String TAG = DBHelper.class.getSimpleName();
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "walnut.db";
    private Context mContext;
    private static DBHelper sInstance;
    private SmsTable mSmsTable;
    private AccountTable mAccountTable;

    private DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = context;
    }

    public static DBHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DBHelper(context);
            sInstance.mAccountTable = AccountTable.getInstance(sInstance);
            sInstance.mSmsTable = SmsTable.getInstance(sInstance);
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        SmsTable.onCreate(database);
        AccountTable.onCreate(database);
    }

    public SmsTable getSmsTable() {
        return this.mSmsTable;
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public ArrayList<ShortSms> getMessagesWithQuery(String query) {
        return this.mSmsTable.getMessagesWithQuery(query);
    }

    public ArrayList<ShortSms> getAllMessagesOfAccount(Account account) {
        return this.mSmsTable.getAllMessagesOfAccount(account);
    }

    public Account getAccountById(int id, boolean excludeDisabled) {
        return this.mAccountTable.getAccountById(id, excludeDisabled);
    }
}
