package com.sms.reading.model;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.sms.reading.SMSApplication;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Yogi on 30/07/2016.
 */
public class ParseSms {

    private static final String TAG;

    static {
        TAG = ParseSms.class.getSimpleName();
    }

   /* public static String ParseSender(String number) {
        number = number.toUpperCase();
        if (number.matches("(?i)[A-Z]{2}-?[!,A-Z,0-9]{1,8}\\s*") || number.matches("(?i)[0-9]{1,7}\\s*")) {
            String[] names = number.split("-");
            if (names.length == 2) {
                return names[1];
            }
            if (names.length == 1) {
                if (number.matches("(?i)[0-9]{1,7}\\s*")) {
                    return names[0];
                }
                SMSApplication.getInstance().setupRules();
                HashMap<String, ArrayList<Rule>> rulesMap = SMSApplication.getInstance().getRules();
                ArrayList<Rule> rules = (ArrayList) rulesMap.get(number.substring(2).trim());
                if (rules == null || rules.isEmpty()) {
                    rules = (ArrayList) rulesMap.get(number.trim());
                    if (!(rules == null || rules.isEmpty())) {
                        return number;
                    }
                }
                return number.substring(2);
            }
        }
        return null;
    }*/


    public static ArrayList<ShortSms> Parse(Context context, String number, String body, Date date) {
        number = number.toUpperCase();
        if (!number.matches("(?i)[A-Z]{2}-?[!,A-Z,0-9]{1,8}\\s*") && !number.matches("(?i)[0-9]{1,7}\\s*")) {
            return null;
        }
        String shortName = "Unknown";
        String[] names = number.split("-");
        if (names.length == 2) {
            return parseSms(context, names[1], number, body, date);
        }
        if (names.length != 1) {
            ArrayList<ShortSms> list = new ArrayList<>();
            list.add(makeUnknownSMS(shortName, number, body, date));
            return list;
        } else if (number.matches("(?i)[0-9]{1,7}\\s*")) {
            return parseSms(context, names[0], number, body, date);
        } else {
            HashMap<String, ArrayList<Rule>> rulesMap = SMSApplication.getInstance().getRules();
            shortName = number.substring(2);
            ArrayList<Rule> rules = (ArrayList) rulesMap.get(shortName.trim());
            if (rules == null || rules.isEmpty()) {
                rules = (ArrayList) rulesMap.get(number.trim());
                if (!(rules == null || rules.isEmpty())) {
                    shortName = number;
                }
            }
            return parseSms(context, shortName, number, body, date);
        }
    }


    private static ShortSms makeUnknownSMS(String smsCategory, String number, String body, Date date) {
        ShortSms sms = new ShortSms(number, body, date);
        sms.setCategories(smsCategory, "Messages");
        sms.setAccountType(99);
        return sms;
    }

    private static ArrayList<ShortSms> parseSms(Context context, String shortName, String number, String origBody, Date date) {
        ShortSms sms = null;
        HashMap<String, ArrayList<Rule>> rulesMap = SMSApplication.getInstance().getRules();
        ArrayList<Rule> rules2 = (ArrayList) rulesMap.get(shortName.trim());
        ArrayList<ShortSms> smsList;
        if (rules2 != null) {
            String body = origBody.replaceAll("\\s{2,}", " ");
            smsList = new ArrayList<>();
            String matcherSmsBody = body;
            while (matcherSmsBody.length() > 0) {
                int i;
                boolean reparse = false;
                for (i = 0; i < rules2.size(); i++) {
                    Rule rule = (Rule) rules2.get(i);
                    JSONObject preProcessor = rule.getPreProcessor();
                    if (preProcessor != null) {
                        try {
                            String string = preProcessor.getString("replace");
                            body = body.replaceAll(string, preProcessor.getString("by"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    Matcher matcher = rule.getPattern().matcher(matcherSmsBody);
                    if (matcher.find()) {
                        matcherSmsBody = matcher.replaceFirst("");
                        Rule matchedRule = rule;
                        Log.d(TAG, "Matched " + matchedRule.getPatternUID());
                        int accType = Account.getAccountTypeInt(matchedRule.getAccountType());
                        if (!(accType == 9999 || accType == 99)) {
                            try {
                                if (matchedRule.getSmsType().equalsIgnoreCase("transaction")) {
                                    //  sms = parseTransaction(matcher, matchedRule, number, origBody, body, date);
                                } else if (matchedRule.getSmsType().equalsIgnoreCase("statement")) {
                                    //   sms = parseStatement(matcher, matchedRule, number, origBody, body, date);
                                } else if (matchedRule.getSmsType().equalsIgnoreCase("event")) {
                                    //    sms = parseEvent(matcher, matchedRule, number, origBody, body, date);
                                } else if (matchedRule.getSmsType().equalsIgnoreCase("walnut")) {
                                    //   sms = parseWalnutSms(context, matcher, matchedRule, origBody);
                                }
                            } catch (RuntimeException e2) {
                                Log.e(TAG, "*** Exception while Parsing SMS : " + shortName + " " + number + " " + body + " : " + date, e2);
                            }
                            if (sms != null) {
                                sms.setRule(matchedRule);
                                smsList.add(sms);
                            }
                            if (rule.isReparse()) {
                                reparse = true;
                            }
                        }
                        if (i != rules2.size()) {
                            if (!reparse) {
                                break;
                            }
                        }
                        break;
                    }
                }
                if (i != rules2.size()) {
                    if (reparse) {
                        break;
                    }
                }
                break;
            }
            if (!smsList.isEmpty() || isBlackListed(context, origBody)) {
                return smsList;
            }
            ShortSms shortSms = new ShortSms(number, origBody, date);
            shortSms.setSmsType(9);
            shortSms.setAccountType(9);
            shortSms.setCategories(rules2.get(0).getName(), "Messages");
            smsList.add(shortSms);
            return smsList;
        } else if (isBlackListed(context, origBody)) {
            return null;
        } else {
            smsList = new ArrayList<>();
            smsList.add(makeUnknownSMS(shortName, number, origBody, date));
            return smsList;
        }
    }


    public static boolean isBlackListed(Context context, String body) {
        String blacklistRegex = SMSApplication.getInstance().getBlackList();
        if (blacklistRegex == null || !body.matches(blacklistRegex)) {
            return false;
        }
        Log.d(TAG, "*** SMS BlackListed ***");
        Matcher matcher = Pattern.compile("(?i)OTP is (\\d{6})").matcher(body);
        if (!matcher.find()) {
            return true;
        }
        String OTP = matcher.group(1);
        Intent intent = new Intent("TestOTP");
        intent.putExtra("OTP", OTP);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        return true;
    }


}
