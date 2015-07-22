package com.shagalalab.marshrutka;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.shagalalab.marshrutka.widget.AppCompatPreferenceActivity;

/**
 * A {@link PreferenceActivity} that presents a set of application settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity
        implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "marshrutka";

    // since we use the preference change initially to populate the summary
    // field, we'll ignore that change at start of the activity
    boolean mBindingPreference;
    private String uiInterface;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Add 'general' preferences, defined in the XML file
        addPreferencesFromResource(R.xml.pref_general);

        getSupportActionBar().setTitle(R.string.action_settings);

        // For all preferences, attach an OnPreferenceChangeListener so the UI summary can be
        // updated when the preference changes.
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_interface_key)));
    }

    /**
     * Attaches a listener so the summary is always updated with the preference value.
     * Also fires the listener once, to initialize the summary (so it shows up before the value
     * is changed.)
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        mBindingPreference = true;

        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);

        // Trigger the listener immediately with the preference's
        // current value.
        onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), ""));

        mBindingPreference = false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        String newValue = value.toString();
        String oldValue = preference.getSharedPreferences().getString(getString(R.string.pref_interface_key), "");

        if (!mBindingPreference && !oldValue.equals(newValue)
            && preference.getKey().equals(getString(R.string.pref_interface_key))) {

                Utils.NEED_RESTART = true;
                finish();
        }

        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list (since they have separate labels/values).
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(newValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else {
            // For other preferences, set the summary to the value's simple string representation.
            preference.setSummary(newValue);
        }

        return true;
    }
}