package com.sms.reading.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sms.reading.model.Account;
import com.sms.reading.model.ShortSms;
import com.sms.reading.model.Transaction;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Yogi on 30/07/2016.
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final String TAG = DBHelper.class.getSimpleName();
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "walnut.db";
    private static DBHelper sInstance;
    private Context mContext;
    private SmsTable mSmsTable;
    private TransactionTable mTransactionTable;
    private AccountTable mAccountTable;

    private DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = context;
    }

    public static DBHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DBHelper(context);
            sInstance.mAccountTable = AccountTable.getInstance(sInstance);
            sInstance.mTransactionTable = TransactionTable.getInstance(sInstance);
            sInstance.mSmsTable = SmsTable.getInstance(sInstance);
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        AccountTable.onCreate(database);
        TransactionTable.onCreate(database);
        SmsTable.onCreate(database);
    }

    public SmsTable getSmsTable() {
        return this.mSmsTable;
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void cleanTables() {
        SQLiteDatabase db = getWritableDatabase();
        this.mSmsTable.refreshTable(db);
    }

    public int updateTransactionMerchant(Transaction txn) {
        return mTransactionTable.updateTransactionMerchant(txn);
    }

    public ArrayList<ShortSms> getTransactions(int[] accIds, int[] txnTypes, String posName, Date startDate, Date endDate, boolean sortAscending) {
        return this.mTransactionTable.getTransactions(accIds, txnTypes, posName, null, startDate, endDate, sortAscending, 0);
    }

    public int updateAccount(Account account, ContentValues values) {
        return this.mAccountTable.updateAccount(account, values);
    }

    public Account getParentAccount(long accountId) {
        return this.mAccountTable.getParentAccount(accountId);
    }

    public ShortSms getSmsByUUID(String UUID) {
        return this.mSmsTable.getSmsByUUID(UUID);
    }

    public ShortSms getSmsById(long _id) {
        return this.mSmsTable.getSmsById(_id);
    }

    public AccountTable getAccountTable() {
        return this.mAccountTable;
    }

    public TransactionTable getTransactionTable() {
        return this.mTransactionTable;
    }
}
