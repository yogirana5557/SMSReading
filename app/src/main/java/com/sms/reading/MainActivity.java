package com.sms.reading;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.sms.reading.db.DBHelper;
import com.sms.reading.model.Account;
import com.sms.reading.model.ShortSms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity {
    private Account account;
    private DBHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.mDbHelper = SMSApplication.getInstance().getDbHelper();

        this.account = this.mDbHelper.getAccountById(5, false);
        new C04251().execute();
    }

    private void showView() {
        boolean z = false;
        ArrayList<ShortSms> smslist =account.getSmsMap().get("Messages");
        Collections.sort(smslist, new Comparator<ShortSms>() {
            @Override
            public int compare(ShortSms txn1, ShortSms txn2) {
                return txn2.getDate().compareTo(txn1.getDate());
            }
        });
        if (this.account.getType() != 10) {
            z = true;
        }
        for (int i=0;i<smslist.size();i++){
            Log.d("SmS",""+smslist.get(i).getBody());
        }
//        this.listView.setAdapter(new SmsAdapter(this, 2130968739, smslist, z));
    }

    class C04251 extends AsyncTask<Void, Void, String> {
        C04251() {
        }

        protected String doInBackground(Void... voids) {
            ArrayList<ShortSms> smslist;
            if (MainActivity.this.account.getType() == 10) {
                smslist = MainActivity.this.mDbHelper.getMessagesWithQuery("select * from walnutSms where parsed=0 and body not like '%missed%' and body not like '%toll%' and body not like '% MB %' and _id in (select _id from walnutSms where body like '% off %' or body like '%offer%' or body like '%free%' or body like '%\\%%' escape '\\') order by date desc;");
            } else {
                smslist = MainActivity.this.mDbHelper.getAllMessagesOfAccount(MainActivity.this.account);
            }
            MainActivity.this.account.addShortSmsList(smslist, "Messages");
            return "Done";
        }

        protected void onPostExecute(String result) {
            if (!MainActivity.this.isFinishing()) {
                MainActivity.this.showView();
            }
        }
    }

}
