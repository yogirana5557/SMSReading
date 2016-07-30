package com.sms.reading.model;

import java.util.regex.Pattern;
import org.json.JSONObject;

public class Rule {
    private static final String TAG;
    private String accOverrideName;
    private String accountType;
    private boolean isExpenseAcc;
    private JSONObject jsonDataFields;
    private String name;
    private Pattern pattern;
    private long patternUID;
    private JSONObject preProcessor;
    private String regex;
    String regexTest;
    private boolean reparse;
    private long senderUID;
    private String smsType;
    private long sortUID;

    public Rule() {
        this.isExpenseAcc = true;
        this.reparse = false;
        this.regexTest = "(?i)X+\\d*(\\d{4}).*debited\\s+with (?:INR|Rs)[\\.:,\\s]*([\\d,]+\\.?\\d{0,2}).*(?:thru|using) (.*?)(?:\\.|\\s)";
    }

    static {
        TAG = Rule.class.getSimpleName();
    }

    public Pattern getPattern() {
        return this.pattern;
    }

    public String getAccountType() {
        return this.accountType;
    }

    public long getSortUID() {
        return this.sortUID;
    }

    public void setSortUID(long sortUID) {
        this.sortUID = sortUID;
    }

    public void setSenderUID(long senderUID) {
        this.senderUID = senderUID;
    }

    public long getPatternUID() {
        return this.patternUID;
    }

    public void setPatternUID(long patternUID) {
        this.patternUID = patternUID;
    }

    public String getSmsType() {
        return this.smsType;
    }

    public JSONObject getJsonDataFields() {
        return this.jsonDataFields;
    }

    public void setRegex(String regex) {
        if (regex.contains("\\\\")) {
            this.regex = regex.replaceAll("\\\\", "\\");
        } else {
            this.regex = regex;
        }
        this.pattern = Pattern.compile(regex);
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public void setSmsType(String smsType) {
        this.smsType = smsType;
    }

    public void setJsonDataFields(JSONObject jsonDataFields) {
        this.jsonDataFields = jsonDataFields;
    }

    public void setPreProcessor(JSONObject preProcessor) {
        this.preProcessor = preProcessor;
    }

    public JSONObject getPreProcessor() {
        return this.preProcessor;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isReparse() {
        return this.reparse;
    }

    public void setReparse(boolean reparse) {
        this.reparse = reparse;
    }

    public boolean isExpenseAccount() {
        return this.isExpenseAcc;
    }

    public void setIsExpenseAcc(boolean isExpenseAcc) {
        this.isExpenseAcc = isExpenseAcc;
    }

    public String getAccOverrideName() {
        return this.accOverrideName;
    }

    public void setAccOverrideName(String accOverrideName) {
        this.accOverrideName = accOverrideName;
    }
}
