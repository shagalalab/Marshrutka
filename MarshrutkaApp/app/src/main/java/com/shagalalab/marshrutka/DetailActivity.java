package com.shagalalab.marshrutka;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shagalalab.marshrutka.data.Route;
import com.shagalalab.marshrutka.db.DbHelper;
import com.shagalalab.marshrutka.widget.PathDrawer;

import org.w3c.dom.Text;


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

        TextView txtTransportType = (TextView)findViewById(R.id.txt_typeoftransport);
        TextView txtTransportNo = (TextView)findViewById(R.id.txt_transportnumber);

        Route currentRoute = DbHelper.getInstance(this).routes[routeId];
        txtTransportType.setText(currentRoute.isBus ? R.string.type_of_transport_bus
                                                    : R.string.type_of_transport_marshrutka);
        txtTransportNo.setText(Integer.toString(currentRoute.displayNo));

        mInflater = LayoutInflater.from(this);

        final LinearLayout txtContainer = (LinearLayout)findViewById(R.id.destination_txt_container);
        txtContainer.addView(generateTextView(currentRoute.pointA.name));
        txtContainer.addView(generateTextView(currentRoute.pointB.name));
        if (currentRoute.pointC != null) {
            txtContainer.addView(generateTextView(currentRoute.pointC.name));
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
