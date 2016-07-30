package com.sms.reading.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.sms.reading.model.Account;
import com.sms.reading.model.AccountBalance;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONException;

public class AccountTable {
    private static String TAG;
    private static AccountTable sInstance;
    private String[] allColumns;
    private SQLiteDatabase database;
    private DBHelper dbHelper;

    static {
        TAG = AccountTable.class.getSimpleName();
    }

    private AccountTable(DBHelper dbh) {
        this.database = null;
        this.allColumns = new String[]{"_id", "name", "displayName", "pan", "displayPan", "type", "flags", "startDate", "endDate", "enabled", "UUID", "modifyCount", "MUUID", "balance", "outstandingBalance", "balLastSyncTime", "outBalLastSyncTime", "updatedTime", "accountColor", "cardIssuer"};
        this.dbHelper = dbh;
    }

    public static AccountTable getInstance(DBHelper dbh) {
        if (sInstance == null) {
            sInstance = new AccountTable(dbh);
            sInstance.database = dbh.getWritableDatabase();
        }
        return sInstance;
    }

    public static void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating Table : create table if not exists walnutAccounts(_id integer primary key autoincrement, name text not null, displayName text, pan text not null,displayPan text,type integer not null, flags integer default 0,startDate integer,endDate integer,enabled boolean default 1,UUID text,modifyCount integer default 1,MUUID text,balance real,outstandingBalance real,balLastSyncTime integer, outBalLastSyncTime integer, updatedTime integer,accountColor integer,cardIssuer text);");
        db.execSQL("create table if not exists walnutAccounts(_id integer primary key autoincrement, name text not null, displayName text, pan text not null,displayPan text,type integer not null, flags integer default 0,startDate integer,endDate integer,enabled boolean default 1,UUID text,modifyCount integer default 1,MUUID text,balance real,outstandingBalance real,balLastSyncTime integer, outBalLastSyncTime integer, updatedTime integer,accountColor integer,cardIssuer text);");
        db.execSQL("create trigger if not exists AccountsTriggerModifiedFlag After update on walnutAccounts for each row  Begin  Update walnutAccounts Set modifyCount = modifyCount + 1  Where _id =  New._id;  End; ");
        db.execSQL("PRAGMA recursive_triggers = false;");
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {

    }

    public Account createAccount(String name, String pan, int type) {
        return createAccount(name, pan, type, true, null);
    }

    public Account createAccount(String name, String pan, int type, boolean isExpenseAccount, String accOverrideName) {
        Cursor cursor = this.database.query("walnutAccounts", this.allColumns, "name=? AND pan=?", new String[]{name, pan}, null, null, null);
        if (cursor == null || cursor.getCount() <= 0) {
            if (cursor != null) {
                cursor.close();
            }
            ContentValues values = new ContentValues();
            values.put("name", name);
            String str = "displayName";
            if (TextUtils.isEmpty(accOverrideName)) {
                accOverrideName = name;
            }
            values.put(str, accOverrideName);
            values.put("pan", pan);
            values.put("displayPan", pan);
            values.put("type", Integer.valueOf(type));
            if (!isExpenseAccount) {
                values.put("flags", Integer.valueOf(16));
            }
            values.put("updatedTime", Long.valueOf(System.currentTimeMillis()));
            values.put("UUID", UUID.randomUUID().toString());
            this.database.beginTransaction();
            cursor = this.database.query("walnutAccounts", this.allColumns, "_id = " + this.database.insertOrThrow("walnutAccounts", null, values), null, null, null, null);
            cursor.moveToFirst();
            Account newAccount = cursorToAccount(cursor);
            cursor.close();
            this.database.setTransactionSuccessful();
            this.database.endTransaction();
            return newAccount;
        }
        cursor.moveToFirst();
        return cursorToAccount(cursor);
    }

    public Account getAccountById(int id, boolean excludeDisabled) {
        String whereClause = "flags & 1 = 0 AND _id = " + id;
        if (excludeDisabled) {
            whereClause = whereClause + " AND enabled = 1";
        }
        Cursor cursor = this.database.query("walnutAccounts", this.allColumns, whereClause, null, null, null, null);
        if (cursor == null || cursor.getCount() <= 0) {
            return null;
        }
        cursor.moveToFirst();
        Account account = cursorToAccount(cursor);
        cursor.close();
        return account;
    }

    public Account getAccountByUUID(String uuid, boolean excludeDisabled) {
        String whereClause = "flags & 1 = 0 AND UUID = '" + uuid + "'";
        if (excludeDisabled) {
            whereClause = whereClause + " AND enabled = 1";
        }
        Cursor cursor = this.database.query("walnutAccounts", this.allColumns, whereClause, null, null, null, null);
        if (cursor == null || cursor.getCount() <= 0) {
            return null;
        }
        cursor.moveToFirst();
        Account account = cursorToAccount(cursor);
        cursor.close();
        return account;
    }

    public Account createAccount(Account account) {
        Cursor cursor = this.database.query("walnutAccounts", this.allColumns, "name=? AND pan=?", new String[]{account.getName(), account.getPan()}, null, null, null);
        if (cursor == null || cursor.getCount() <= 0) {
            if (cursor != null) {
                cursor.close();
            }
            ContentValues values = new ContentValues();
            values.put("name", account.getName());
            values.put("displayName", account.getDisplayName());
            values.put("pan", account.getPan());
            values.put("displayPan", account.getDisplayPan());
            values.put("type", Integer.valueOf(account.getType()));
            values.put("startDate", Integer.valueOf(account.getStartDate()));
            values.put("endDate", Integer.valueOf(account.getEndDate()));
            values.put("UUID", UUID.randomUUID().toString());
            values.put("updatedTime", Long.valueOf(account.getUpdatedDate()));
            values.put("accountColor", Integer.valueOf(account.getColorIndex()));
            putBalance(values, account.getBalanceInfo());
            cursor = this.database.query("walnutAccounts", this.allColumns, "_id = " + this.database.insert("walnutAccounts", null, values), null, null, null, null);
            cursor.moveToFirst();
            Account newAccount = cursorToAccount(cursor);
            cursor.close();
            return newAccount;
        }
        cursor.moveToFirst();
        cursor.close();
        return cursorToAccount(cursor);
    }



    public int updateAccount(Account account, ContentValues values) {
        if (account.get_id() >= 0) {
            return this.database.update("walnutAccounts", values, "_id = " + account.get_id(), null);
        }
        return -1;
    }



    public void updateAccountsExpenseState(String accountIds, boolean isExpense) {
        if (!TextUtils.isEmpty(accountIds)) {
            StringBuilder flag = new StringBuilder();
            if (isExpense) {
                flag.append("flags = flags & -17");
            } else {
                flag.append("flags = flags | 16");
            }
            flag.append(", updatedTime=" + System.currentTimeMillis());
            this.database.execSQL("update walnutAccounts set " + flag + " where " + "_id" + " IN ( " + accountIds + " )");
        }
    }

    public void updateAccountsEnableState(String accountIds, boolean enable) {
        if (!TextUtils.isEmpty(accountIds)) {
            StringBuilder flag = new StringBuilder();
            flag.append("enabled = '" + (enable ? 1 : 0) + "', " + "updatedTime" + "=" + System.currentTimeMillis());
            this.database.execSQL("update walnutAccounts set " + flag + " where " + "_id" + " IN ( " + accountIds + " )");
        }
    }




    public int updateAccountSetColor(long accountId, int colorIndex) {
        if (accountId >= 0) {
            ContentValues values = new ContentValues();
            values.put("accountColor", Integer.valueOf(colorIndex));
            values.put("updatedTime", Long.valueOf(System.currentTimeMillis()));
            return this.database.update("walnutAccounts", values, "_id = " + accountId, null);
        }
        Log.d(TAG, "Did not update Account : " + accountId);
        return -1;
    }

    public ArrayList<Account> getAllAccounts() {
        ArrayList<Account> accounts = new ArrayList();
        Cursor cursor = this.database.query("walnutAccounts", this.allColumns, "flags & 1 = 0 AND flags & 2 = 0", null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            accounts.add(cursorToAccount(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return accounts;
    }

    public ArrayList<Account> getEnabledAccounts() {
        ArrayList<Account> accounts = new ArrayList();
        Cursor cursor = this.database.query("walnutAccounts", this.allColumns, "flags & 1 = 0 AND flags & 2 = 0 AND enabled = 1 AND type != 13 AND type != 5", null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            accounts.add(cursorToAccount(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return accounts;
    }

    public static ArrayList<String> getDisabledAccountIds(SQLiteDatabase database) {
        ArrayList<String> accounts = new ArrayList();
        SQLiteDatabase sQLiteDatabase = database;
        Cursor cursor = sQLiteDatabase.query("walnutAccounts", new String[]{"_id"}, "flags & 1 = 0 AND enabled = 0 ", null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            accounts.add(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("_id"))));
            cursor.moveToNext();
        }
        cursor.close();
        return accounts;
    }

    public long getCount() {
        return DatabaseUtils.queryNumEntries(this.database, "walnutAccounts");
    }

    public static Account cursorToAccount(Cursor c) {
        boolean z = true;
        Account account = new Account(c.getString(c.getColumnIndexOrThrow("name")), c.getString(c.getColumnIndexOrThrow("pan")), c.getInt(c.getColumnIndexOrThrow("type")));
        account.setDisplayName(c.getString(c.getColumnIndexOrThrow("displayName")));
        account.setDisplayPan(c.getString(c.getColumnIndexOrThrow("displayPan")));
        account.set_id(c.getInt(c.getColumnIndexOrThrow("_id")));
        account.setFlags(c.getInt(c.getColumnIndexOrThrow("flags")));
        account.setUuid(c.getString(c.getColumnIndexOrThrow("UUID")));
        account.setMUUID(c.getString(c.getColumnIndexOrThrow("MUUID")));
        if (c.getInt(c.getColumnIndexOrThrow("enabled")) != 1) {
            z = false;
        }
        account.setEnabled(z);
        account.setStartDate(c.getInt(c.getColumnIndexOrThrow("startDate")));
        account.setEndDate(c.getInt(c.getColumnIndexOrThrow("endDate")));
        account.setUpdatedDate(c.getLong(c.getColumnIndexOrThrow("updatedTime")));
        account.setBalanceInfo(cursorToAccountBalance(c));
        account.setColorIndex(c.getInt(c.getColumnIndexOrThrow("accountColor")));
        account.setUuid(c.getString(c.getColumnIndexOrThrow("UUID")));
        account.setCardIssuer(c.getString(c.getColumnIndexOrThrow("cardIssuer")));
        return account;
    }

    private static AccountBalance cursorToAccountBalance(Cursor c) {
        AccountBalance balance = new AccountBalance();
        balance.setBalance(c.getDouble(c.getColumnIndexOrThrow("balance")));
        balance.setOutstandingBalance(c.getDouble(c.getColumnIndexOrThrow("outstandingBalance")));
        long balsyncTime = c.getLong(c.getColumnIndexOrThrow("balLastSyncTime"));
        long outBalsyncTime = c.getLong(c.getColumnIndexOrThrow("outBalLastSyncTime"));
        if (balsyncTime != 0) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(balsyncTime);
            balance.setBalSyncDate(cal.getTime());
        }
        if (outBalsyncTime != 0) {
            Calendar   cal = Calendar.getInstance();
            cal.setTimeInMillis(outBalsyncTime);
            balance.setOutbalSyncdate(cal.getTime());
        }
        return balance;
    }

    public void refreshTable(SQLiteDatabase database) {
        database.beginTransaction();
        database.execSQL("drop table if exists walnutAccounts");
        database.execSQL("drop trigger if exists AccountsTriggerModifiedFlag");
        onCreate(database);
        database.setTransactionSuccessful();
        database.endTransaction();
    }

    public String getDisabledAccountUUIDsForBackup() {
        Cursor cursor = this.database.query("walnutAccounts", new String[]{"UUID"}, "enabled = 0 ", null, null, null, null);
        JSONArray jsonList = new JSONArray();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            jsonList.put(cursor.getString(cursor.getColumnIndexOrThrow("UUID")));
            cursor.moveToNext();
        }
        cursor.close();
        return jsonList.toString();
    }

    public void setDisabledAccountsByUUID(String uuids) {
        try {
            JSONArray jsonArray = new JSONArray(uuids);
            ContentValues values = new ContentValues();
            values.put("enabled", Integer.valueOf(0));
            for (int i = 0; i < jsonArray.length(); i++) {
                String uuid = jsonArray.getString(i);
                this.database.update("walnutAccounts", values, "UUID =?", new String[]{uuid});
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }





    public String getAccountUUID(long acc_id) {
        String UUID = null;
        String[] strArr = new String[]{"UUID"};
        Cursor cursor = this.database.query("walnutAccounts", strArr, "_id = " + acc_id, null, null, null, null);
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            UUID = cursor.getString(cursor.getColumnIndexOrThrow("UUID"));
        }
        cursor.close();
        return UUID;
    }

    private String getAccountMUUID(long acc_id) {
        String MUUID = null;
        String[] strArr = new String[]{"MUUID"};
        Cursor cursor = this.database.query("walnutAccounts", strArr, "_id = " + acc_id, null, null, null, null);
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            MUUID = cursor.getString(cursor.getColumnIndexOrThrow("MUUID"));
        }
        cursor.close();
        return MUUID;
    }

    public ArrayList<Account> getAccountsByName(String accName) {
        ArrayList<Account> accounts = new ArrayList();
        Cursor cursor = this.database.query("walnutAccounts", this.allColumns, "name LIKE '" + accName + "%'" + " AND " + "flags" + " & " + 1 + " = 0 AND " + "flags" + " & " + 2 + " = 0" + " AND (" + "type" + " = " + 1 + " OR " + "type" + " = " + 2 + " OR " +"type" + " = " + 3 + " )", null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                accounts.add(cursorToAccount(cursor));
                cursor.moveToNext();
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return accounts;
    }

    public ArrayList<Account> getLinkedAccount(long accountId) {
        ArrayList<Account> accounts = new ArrayList();
        Cursor cursor = this.database.query("walnutAccounts", this.allColumns, "_id IN (" + getHierarchicalChildAccountIdQuery(new int[]{(int) accountId}, true) + ")", null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                accounts.add(cursorToAccount(cursor));
                cursor.moveToNext();
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return accounts;
    }

    public static void putBalance(ContentValues values, AccountBalance balance) {
        if (balance != null) {
            if (balance.getBalance() != Double.MIN_VALUE) {
                values.put("balance", Double.valueOf(balance.getBalance()));
                if (balance.getBalSyncDate() != null) {
                    values.put("balLastSyncTime", Long.valueOf(balance.getBalSyncDate().getTime()));
                }
            }
            if (balance.getOutstandingBalance() != Double.MIN_VALUE) {
                values.put("outstandingBalance", Double.valueOf(balance.getOutstandingBalance()));
                if (balance.getOutbalSyncdate() != null) {
                    values.put("outBalLastSyncTime", Long.valueOf(balance.getOutbalSyncdate().getTime()));
                }
            }
        }
    }

    public String getRecursiveChildAccountIdsAsString(int depth, String uuid) {
        Cursor childAccIds = this.database.query("walnutAccounts", new String[]{"UUID"}, "MUUID = '" + uuid + "'", null, null, null, null);
        String recursiveAccIds = "";
        while (childAccIds.moveToNext()) {
            recursiveAccIds = recursiveAccIds + getRecursiveChildAccountIdsAsString(depth + 1, childAccIds.getString(childAccIds.getColumnIndex("UUID")));
        }
        if (childAccIds != null) {
            childAccIds.close();
        }
        recursiveAccIds = recursiveAccIds + "'" + uuid + "'";
        if (depth > 0) {
            return recursiveAccIds + ", ";
        }
        return "(" + recursiveAccIds + ")";
    }

    public String getHierarchicalChildAccountIdQuery(int[] accIds, boolean excludeSelf) {
        int length = accIds.length;
        int i = 0;
        while (i < length) {
            int id = accIds[i];
            String[] strArr = new String[]{"_id"};
            Cursor cursor = this.database.query("walnutAccounts", strArr, "UUID IN " + getRecursiveChildAccountIdsAsString(0, getAccountUUID((long) id)), null, null, null, null);
            if (cursor == null || cursor.getCount() <= 0) {
                if (cursor != null) {
                    cursor.close();
                }
                i++;
            } else {
                ArrayList<String> aIds = new ArrayList();
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    aIds.add(cursor.getString(cursor.getColumnIndexOrThrow("_id")));
                    cursor.moveToNext();
                }
                if (excludeSelf) {
                    aIds.remove(String.valueOf(id));
                }
                String childIds = makeCommaSeparatedString((String[]) aIds.toArray(new String[aIds.size()]));
                cursor.close();
                return childIds;
            }
        }
        return makeCommaSeparatedString(accIds);
    }

    public String getRecursiveParentAccountIdsAsString(int depth, String uuid) {
        Cursor childAccIds = this.database.query("walnutAccounts", new String[]{"MUUID"}, "UUID = '" + uuid + "'", null, null, null, null);
        while (childAccIds.moveToNext()) {
            getRecursiveChildAccountIdsAsString(depth + 1, childAccIds.getString(childAccIds.getColumnIndex("MUUID")));
        }
        if (childAccIds != null) {
            childAccIds.close();
        }
        return uuid;
    }

    public String getHierarchicalParentAccountIdQuery(long accountId) {
        String muuid = getAccountMUUID(accountId);
        if (!TextUtils.isEmpty(muuid)) {
            String[] strArr = new String[]{"_id"};
            Cursor cursor = this.database.query("walnutAccounts", strArr, "UUID = '" + getRecursiveParentAccountIdsAsString(0, muuid) + "'", null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                String parentId = cursor.getString(cursor.getColumnIndexOrThrow("_id"));
                cursor.close();
                return parentId;
            } else if (cursor != null) {
                cursor.close();
            }
        }
        return String.valueOf(accountId);
    }

    private static String makeCommaSeparatedString(String[] data) {
        if (data == null || data.length <= 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder((data.length * 2) - 1);
        sb.append(data[0]);
        for (int i = 1; i < data.length; i++) {
            sb.append("," + data[i]);
        }
        return sb.toString();
    }

    private static String makeCommaSeparatedString(int[] data) {
        if (data == null || data.length <= 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder((data.length * 2) - 1);
        sb.append(data[0]);
        for (int i = 1; i < data.length; i++) {
            sb.append("," + data[i]);
        }
        return sb.toString();
    }
}
