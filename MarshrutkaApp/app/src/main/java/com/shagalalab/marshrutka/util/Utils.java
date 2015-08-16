package com.shagalalab.marshrutka.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.shagalalab.marshrutka.R;

/**
 * Created by aziz on 7/20/15.
 */
public class Utils {
    public static boolean NEED_RESTART = false;
    public static boolean NEED_REFRESH_AUTOCOMPLETETEXT = false;

    public static void pendingRestartApp(Context context, Intent intent) {
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId,
                intent, PendingIntent.FLAG_CANCEL_CURRENT|PendingIntent.FLAG_ONE_SHOT);
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);
    }

    /**
     * Get current version of local DB.
     * Note that even though SQLiteOpenHelper is supposed to track db version,
     * we use database in non-standard way, i.e. we copy db from assets instead of
     * creating it in onCreate() callback, and local DB's version will always be 0.
     * Therefore we need to track it manually.
     *
     * @param context
     * @return
     */
    public static int getDbVersion(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(context.getString(R.string.pref_dbversion_key), 1);
    }

    public static void setDbVersion(Context context, int newDbVersion) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(context.getString(R.string.pref_dbversion_key), newDbVersion);
        editor.commit();
    }
}
