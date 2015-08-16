package com.shagalalab.marshrutka.activity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.shagalalab.marshrutka.R;

public class AboutActivity extends AppCompatActivity {
    private static String WEB_SITE = "http://www.shagalalab.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        TextView tvDescription = (TextView) findViewById(R.id.description);
        tvDescription.setMovementMethod(LinkMovementMethod.getInstance());

        TextView txtAppVersionName = (TextView)findViewById(R.id.txt_app_name_version);
        String appVersionName = String.format(getString(R.string.about_app_name), getVersionName());
        txtAppVersionName.setText(appVersionName);
    }

    private String getVersionName() {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
