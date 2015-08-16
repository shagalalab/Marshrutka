package com.shagalalab.marshrutka.activity;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shagalalab.marshrutka.App;
import com.shagalalab.marshrutka.R;
import com.shagalalab.marshrutka.data.DestinationPoint;
import com.shagalalab.marshrutka.data.Route;
import com.shagalalab.marshrutka.db.DbHelper;
import com.shagalalab.marshrutka.widget.PathDrawer;

import java.util.ArrayList;


public class DetailActivity extends AppCompatActivity {

    public static final String ROUTE_ID = "ROUTE_ID";
    public static final String CHOSEN_DESTINATIONS_INTERVAL = "CHOSEN_DESTINATIONS_INTERVAL";
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

        int[] chosenDestinations = getIntent().getIntArrayExtra(CHOSEN_DESTINATIONS_INTERVAL);

        TextView txtTransportType = (TextView)findViewById(R.id.txt_typeoftransport);
        TextView txtTransportNo = (TextView)findViewById(R.id.txt_transportnumber);

        Route currentRoute = DbHelper.getInstance(this).routes[routeId];
        txtTransportType.setText(currentRoute.isBus ? R.string.type_of_transport_bus
                                                    : R.string.type_of_transport_marshrutka);
        txtTransportNo.setText(Integer.toString(currentRoute.displayNo));

        mInflater = LayoutInflater.from(this);

        final LinearLayout txtContainer = (LinearLayout)findViewById(R.id.destination_txt_container);
        ArrayList<DestinationPoint> pathPoints = currentRoute.pathPoints;

        final int[] selectionIndices = getSelectionIndices(pathPoints, chosenDestinations);

        int len = pathPoints.size();
        for (int i=0; i<len; i++) {
            DestinationPoint destinationPoint = pathPoints.get(i);
            boolean makeItalic = selectionIndices[0] >= 0 && i >= selectionIndices[0] && i <= selectionIndices[1];
            txtContainer.addView(generateTextView(destinationPoint.getName(), makeItalic));
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

    private int[] getSelectionIndices(ArrayList<DestinationPoint> pathPoints, int[] chosenDestinations) {
        int[] indices = new int[] { -1, -1 };
        if (chosenDestinations == null) {
            return indices;
        }
        int len = pathPoints.size();
        if (chosenDestinations.length == 1) {
            for (int i=0; i<len; i++) {
                DestinationPoint destinationPoint = pathPoints.get(i);
                if (chosenDestinations[0] == destinationPoint.ID) {
                    return new int[] { i, i};
                }
            }
            Log.e(App.TAG, "Something went wrong inside getSelectionIndices()"
                        +", chosenDestinations[0]="+chosenDestinations[0]
                        +", but that ID is not found in pathPoints");
        }
        if (chosenDestinations.length == 2) {
            for (int i=0; i<len; i++) {
                DestinationPoint destinationPoint = pathPoints.get(i);
                if ((chosenDestinations.length > 0 && chosenDestinations[0] == destinationPoint.ID)
                        || (chosenDestinations.length > 1 && chosenDestinations[1] == destinationPoint.ID)) {
                    if (indices[0] == -1) {
                        indices[0] = i;
                    } else if (indices[1] == -1) {
                        indices[1] = i;

                        if (indices[0] > indices[1]) {
                            int temp = indices[0];
                            indices[0] = indices[1];
                            indices[1] = temp;
                        }
                        break;
                    }
                }
            }
        }
        return indices;
    }

    private TextView generateTextView(String text, boolean makeItalic) {
        TextView txt = (TextView)mInflater.inflate(R.layout.destination_text_detail, null);
        txt.setText(text);
        if (makeItalic) {
            txt.setTypeface(null, Typeface.ITALIC);
        }
        return txt;
    }
}
