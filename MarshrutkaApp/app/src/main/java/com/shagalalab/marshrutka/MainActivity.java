package com.shagalalab.marshrutka;

import java.util.Locale;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.shagalalab.marshrutka.widget.SlidingTabLayout;


public class MainActivity extends AppCompatActivity {

    public final static String TAG = "marshrutka";
    public final static String DEVELOPER_PLAY_LINK = "market://search?q=pub:Shag'ala Lab";

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    private String[] TAB_NAMES;
    String uiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        uiInterface = prefs.getString(getString(R.string.pref_interface_key),
                getString(R.string.pref_interface_default));

        if (!uiInterface.equals(getString(R.string.pref_interface_default))) {
            Utility.changeLocale(this);
        }

        setContentView(R.layout.activity_main);

        TAB_NAMES = new String[] {
                getString(R.string.tab_text_numero_sign),
                getString(R.string.tab_text_from_to)
        };

        // Set up toolbar.
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        final SlidingTabLayout slidingTabLayout =
                (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        slidingTabLayout.setCustomTabView(R.layout.tab_indicator, android.R.id.text1);

        Resources res = getResources();
        slidingTabLayout.setSelectedIndicatorColors(
                res.getColor(R.color.tab_selected_strip));
        slidingTabLayout.setDistributeEvenly(true);
        slidingTabLayout.setViewPager(mViewPager);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        } else if (id == R.id.action_other_apps) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(DEVELOPER_PLAY_LINK));
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return new QueryByNumberFragment();
            } else {
                return new QueryByDestinationsFragment();
            }
        }

        @Override
        public int getCount() {
            return TAB_NAMES.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TAB_NAMES[position];
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.w(TAG, "onConfigurationChanged");
        super.onConfigurationChanged(newConfig);

        if (!uiInterface.equals(getString(R.string.pref_interface_default))) {
            Utility.changeLocale(this);
        }
    }
}
