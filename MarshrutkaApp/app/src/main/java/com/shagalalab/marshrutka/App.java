package com.shagalalab.marshrutka;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;

import java.util.Locale;

/**
 * App-wide lifecycle events
 */
public class App extends Application {
    Locale CYRILLIC, LATIN;
    boolean isLocaleCyrillic = true;

    @Override
    public void onCreate() {
        super.onCreate();
        CYRILLIC = Locale.getDefault();
        LATIN = new Locale("mt");
    }

    public void changeLocaleIfNeeded() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String uiInterface = prefs.getString(getString(R.string.pref_interface_key),
                getString(R.string.pref_interface_default));

        Locale locale;
        if (uiInterface.equals(getString(R.string.pref_interface_default))) {
            locale = CYRILLIC;
        } else {
            locale = LATIN;
        }
        isLocaleCyrillic = (locale == CYRILLIC);
        Locale.setDefault(locale);
        Configuration config = getResources().getConfiguration();
        config.locale = locale;
        getResources().updateConfiguration(config,
                getResources().getDisplayMetrics());
    }

    public boolean isCurrentLocaleCyrillic() {
        return isLocaleCyrillic;
    }
}
