package com.shagalalab.marshrutka;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shagalalab.marshrutka.data.Route;
import com.shagalalab.marshrutka.db.DbHelper;
import com.shagalalab.marshrutka.widget.PathDrawer;


public class DetailActivity extends AppCompatActivity {

    public static final String ROUTE_ID = "ROUTE_ID";
    private static final String TAG = "marshrutka";

    private LayoutInflater mInflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Set up toolbar.
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        int routeId = getIntent().getIntExtra(ROUTE_ID, -1);
        if (routeId == -1) {
            Log.e(TAG, "Intent extra (ROUTE_ID) is not provided");
            return;
        }

        boolean isInterfaceCyrillic = ((App)getApplicationContext()).isCurrentLocaleCyrillic();

        TextView txtTransportType = (TextView)findViewById(R.id.txt_typeoftransport);
        TextView txtTransportNo = (TextView)findViewById(R.id.txt_transportnumber);

        Route currentRoute = DbHelper.getInstance(this).routes[routeId];
        txtTransportType.setText(currentRoute.isBus ? R.string.type_of_transport_bus
                                                    : R.string.type_of_transport_marshrutka);
        txtTransportNo.setText(Integer.toString(currentRoute.displayNo));

        mInflater = LayoutInflater.from(this);

        final LinearLayout txtContainer = (LinearLayout)findViewById(R.id.destination_txt_container);
        txtContainer.addView(generateTextView(currentRoute.pointA.getName(isInterfaceCyrillic)));
        txtContainer.addView(generateTextView(currentRoute.pointB.getName(isInterfaceCyrillic)));
        if (currentRoute.pointC != null) {
            txtContainer.addView(generateTextView(currentRoute.pointC.getName(isInterfaceCyrillic)));
        }
        final PathDrawer pathDrawer = (PathDrawer)findViewById(R.id.path_drawer);
        txtContainer.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                txtContainer.getViewTreeObserver().removeOnPreDrawListener(this);

                pathDrawer.initDrawing(txtContainer);
                pathDrawer.invalidate();
                return true;
            }
        });
    }

    private TextView generateTextView(String text) {
        TextView txt = (TextView)mInflater.inflate(R.layout.destination_text_detail, null);
        txt.setText(text);
        return txt;
    }
}
