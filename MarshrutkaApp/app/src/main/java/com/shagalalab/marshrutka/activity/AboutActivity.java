package com.shagalalab.marshrutka.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.shagalalab.marshrutka.BuildConfig;
import com.shagalalab.marshrutka.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        TextView tvDescription = (TextView) findViewById(R.id.description);
        tvDescription.setMovementMethod(LinkMovementMethod.getInstance());

        TextView txtAppVersionName = (TextView) findViewById(R.id.txt_app_name_version);
        String appVersionName = String.format(getString(R.string.about_app_name), BuildConfig.VERSION_NAME);
        txtAppVersionName.setText(appVersionName);
    }
}
