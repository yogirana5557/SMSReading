package com.sms.reading;

import android.app.Application;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.sms.reading.db.DBHelper;
import com.sms.reading.model.Rule;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Yogi on 30/07/2016.
 */
public class SMSApplication extends Application {

    public static final String TAG = SMSApplication.class.getSimpleName();
    private static SMSApplication mInstance;
    private final Object ruleFileLock;
    ;
    private HashMap<String, ArrayList<Rule>> mRules;
    private String bankAliasRegex;
    private String blackList;
    private String cardAliasRegex;
    private DBHelper mDBHelper;

    public SMSApplication() {
        this.mRules = null;
        this.ruleFileLock = new Object();
    }

    public static synchronized SMSApplication getInstance() {
        SMSApplication walnutApp;
        synchronized (SMSApplication.class) {
            walnutApp = mInstance;
        }
        return walnutApp;
    }

    public static void broadcastReadSmsPermissionRequest(LocalBroadcastManager localBroadcastManager) {
        localBroadcastManager.sendBroadcast(new Intent("walnut.app.REQUEST_FOR_READ_SMS_PERM"));
    }

    public static void broadcastProgress(LocalBroadcastManager localBroadcastManager, String progress) {
        Intent intent = new Intent("walnut.app.WALNUT_PROGRESS");
        intent.putExtra("walnut.app.WALNUT_PROGRESS_EXTRA_STRING", progress);
        localBroadcastManager.sendBroadcast(intent);
    }

    public static void broadcastToast(LocalBroadcastManager localBroadcastManager, String msg) {
        Intent intent = new Intent("walnut.app.WALNUT_TOAST");
        intent.putExtra("walnut.app.WALNUT_TOAST_EXTRA_MSG", msg);
        localBroadcastManager.sendBroadcast(intent);
    }

    public static void broadcastFinish(LocalBroadcastManager localBroadcastManager) {
        localBroadcastManager.sendBroadcast(new Intent("walnut.app.WALNUT_FINISH"));
    }

    public synchronized DBHelper getDbHelper() {
        return this.mDBHelper;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        mDBHelper.close();
    }

    private String copyStreams(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        Writer writer = new StringWriter();
        while (true) {
            int read = in.read(buffer);
            if (read == -1) {
                return writer.toString();
            }
            if (out != null) {
                out.write(buffer, 0, read);
            }
            writer.write(new String(buffer), 0, read);
        }
    }

    public void setupRules() {
        synchronized (this.ruleFileLock) {
            if (this.mRules == null) {
                try {
                    InputStream fis = getAssets().open("rules.json");
                    String rulesJson = copyStreams(fis, null);
                    fis.close();
                    setupRulesMap(rulesJson);
                } catch (IOException e) {
                    Log.e(TAG, "Exception setting up Rules", e);
                }
            }
        }
    }


    private boolean setupRulesMap(String rulesString) {
        try {
            this.mRules = new HashMap<>();
            JSONObject jSONObject = new JSONObject(rulesString);
            Log.d(TAG, "Using json Rules version : " + jSONObject.getString("version"));
            JSONArray rulesJArray = jSONObject.getJSONArray("rules");
            this.blackList = jSONObject.optString("blacklist_regex");
            this.cardAliasRegex = jSONObject.optString("card_alias_regex");
            this.bankAliasRegex = jSONObject.optString("bank_alias_regex");
            for (int i = 0; i < rulesJArray.length(); i++) {
                int j;
                JSONObject senderRuleJObj = rulesJArray.getJSONObject(i);
                String name = senderRuleJObj.optString("name", "Unknown");
                long senderUID = senderRuleJObj.getLong("sender_UID");
                boolean isExpenseAcc = senderRuleJObj.optBoolean("set_account_as_expense", true);
                JSONArray patternsJArray = senderRuleJObj.getJSONArray("patterns");
                JSONObject preProcessor = senderRuleJObj.optJSONObject("sms_preprocessor");
                ArrayList<Rule> rulesArray = new ArrayList<>();
                for (j = 0; j < patternsJArray.length(); j++) {
                    JSONObject patternJObj = patternsJArray.getJSONObject(j);
                    Rule rule = new Rule();
                    rule.setRegex(patternJObj.getString("regex"));
                    rule.setAccountType(patternJObj.getString("account_type"));
                    rule.setSmsType(patternJObj.getString("sms_type"));
                    rule.setAccOverrideName(patternJObj.optString("account_name_override"));
                    rule.setPatternUID(patternJObj.getLong("pattern_UID"));
                    rule.setSortUID(patternJObj.getLong("sort_UID"));
                    rule.setSenderUID(senderUID);
                    rule.setReparse(patternJObj.optBoolean("reparse", false));
                    rule.setJsonDataFields(patternJObj.optJSONObject("data_fields"));
                    rule.setPreProcessor(preProcessor);
                    rule.setName(name);
                    if (patternJObj.has("set_account_as_expense")) {
                        rule.setIsExpenseAcc(patternJObj.optBoolean("set_account_as_expense", true));
                    } else {
                        rule.setIsExpenseAcc(isExpenseAcc);
                    }
                    rulesArray.add(rule);
                }
                Collections.sort(rulesArray, new Comparator<Rule>() {
                    @Override
                    public int compare(Rule rule1, Rule rule2) {
                        return (int) (rule1.getSortUID() - rule2.getSortUID());
                    }
                });
                JSONArray senderJArray = senderRuleJObj.getJSONArray("senders");
                for (j = 0; j < senderJArray.length(); j++) {
                    this.mRules.put(senderJArray.getString(j), rulesArray);
                }
            }
            return true;
        } catch (JSONException e) {
            Log.e(TAG, "Error validating rules!", e);
            return false;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        this.mDBHelper = DBHelper.getInstance(this);
    }

    public HashMap<String, ArrayList<Rule>> getRules() {
        return mRules;
    }

    public String getBlackList() {
        return this.blackList;
    }

}
