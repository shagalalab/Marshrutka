package com.shagalalab.marshrutka.db;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.shagalalab.marshrutka.data.DestinationPoint;
import com.shagalalab.marshrutka.data.ReachableDestinations;
import com.shagalalab.marshrutka.data.ReverseRoute;
import com.shagalalab.marshrutka.data.Route;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DbHelper extends SQLiteOpenHelper {
    private static final String TAG = "marshrutka";

    private static final String TABLE_ROUTES = "routes";
    private static final String COLUMN_ROUTES_ID = "id";
    private static final String COLUMN_ROUTES_TYPE = "type";
    private static final String COLUMN_ROUTES_DISPLAYNO = "displaynumber";
    private static final String COLUMN_ROUTES_POINTA = "pointA";
    private static final String COLUMN_ROUTES_POINTB = "pointB";
    private static final String COLUMN_ROUTES_POINTC = "pointC";

    private static final String TABLE_DESTINATIONS = "destinations";
    private static final String COLUMN_DESTINATIONS_ID = "id";
    private static final String COLUMN_DESTINATIONS_NAME_CYR = "name_cyr";
    private static final String COLUMN_DESTINATIONS_NAME_LAT = "name_lat";

    private static final String TABLE_REVERSEROUTES = "reverseroutes";
    private static final String COLUMN_REVERSEROUTES_DESTIONATIONID = "destinationId";
    private static final String COLUMN_REVERSEROUTES_ROUTEIDS = "routeIds";

    private static final String TABLE_REACHABLEDESTINATIONS = "reachabledestinations";
    private static final String COLUMN_REACHABLEDESTINATIONS_DESTINATIONID = "destinationId";
    private static final String COLUMN_REACHABLEDESTINATIONS_REACHABLEDESTINATIONIDS = "reachableDestinationIds";

    public DestinationPoint[] destinationPoints;
    public ReverseRoute[] reverseRoutes;
    public Route[] routes;
    public ReachableDestinations[] reachableDestinations;

    //The Android's default system path of your application database.
    private final String DB_PATH;

    private static final String DB_NAME = "marshrutka.db";
    private static final int DB_VERSION = 1;

    private SQLiteDatabase mDataBase;

    private final Context mContext;

    /**
     * Constructor
     * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
     *
     * @param context
     */
    DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.mContext = context;
        DB_PATH = String.format("/data/data/%s/databases/", context.getPackageName());
    }

    private static DbHelper _instance;

    public static DbHelper getInstance(Context context) {
        if (_instance == null) {
            _instance = new DbHelper(context);
            _instance.loadAllData();
        }
        return _instance;
    }

    /**
     * Creates a empty database on the system and rewrites it with your own database.
     */
    public void createDataBaseIfNotExists() throws IOException {

        boolean dbExist = checkDataBase();

        if (dbExist) {
            //do nothing - database already exist
        } else {

            //By calling this method and empty database will be created into the default system path
            //of your application so we are gonna be able to overwrite that database with our database.
            this.getReadableDatabase();

            try {
                copyDataBase();
            } catch (IOException e) {
                throw new Error("Error copying database");
            }
        }
    }

    private void removeObsoleteDatabase() {
        File dbfile = new File(DB_PATH + DB_NAME);
        if (dbfile.exists()) {
            dbfile.delete();
        }
    }

    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     *
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase() {
        return new File(DB_PATH + DB_NAME).exists();
    }

    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     */
    private void copyDataBase() throws IOException {

        //Open your local db as the input stream
        InputStream myInput = mContext.getAssets().open(DB_NAME);

        // Path to the just created empty db
        String outFileName = DB_PATH + DB_NAME;

        //Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);

        //transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }

        //Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    public void openDataBase() throws SQLException {
        String dbPath = DB_PATH + DB_NAME;
        mDataBase = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY);
    }

    @Override
    public synchronized void close() {

        if (mDataBase != null)
            mDataBase.close();

        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "DbHelper.onCreate()");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "DbHelper.onUpgrade() oldVersion="+oldVersion+", newVersion="+newVersion);
        removeObsoleteDatabase();
        try {
            createDataBaseIfNotExists();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Add your public helper methods to access and get content from the database.
    // You could return cursors by doing "return mDataBase.query(....)" so it'd be easy
    // to you to create adapters for your views.

    private void loadAllData() {
        if (routes != null && destinationPoints != null && reverseRoutes != null) {
            // nothing to do
            return;
        }
        try {
            createDataBaseIfNotExists();
            openDataBase();

            loadDestinationPoints();
            loadRoutes();
            loadReverseRoutes();
            loadReachableDestinations();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadRoutes() {
        int count = getCount(TABLE_ROUTES);
        routes = new Route[count];
        Cursor cursor = mDataBase.query(TABLE_ROUTES,
                        new String[] {
                            COLUMN_ROUTES_ID,
                            COLUMN_ROUTES_TYPE,
                            COLUMN_ROUTES_DISPLAYNO,
                            COLUMN_ROUTES_POINTA,
                            COLUMN_ROUTES_POINTB,
                            COLUMN_ROUTES_POINTC
                        }, null, null, null, null, null);

        while (cursor.moveToNext()) {
            Route route = new Route();
            route.ID = cursor.getInt(0);
            route.isBus = cursor.getInt(1) == 1;
            route.displayNo = cursor.getInt(2);
            // Be careful, IDs in DB start from 1, while we will access destinationPoints
            // by array index that start from 0
            route.pointA = destinationPoints[cursor.getInt(3)];
            route.pointB = destinationPoints[cursor.getInt(4)];
            int pointC = cursor.getInt(5);
            if (pointC != -1) {
                route.pointC = destinationPoints[cursor.getInt(5)];
            }

            routes[route.ID] = route;
        }
        cursor.close();
    }

    private void loadDestinationPoints() {
        int count = getCount(TABLE_DESTINATIONS);
        destinationPoints = new DestinationPoint[count];
        Cursor cursor = mDataBase.query(TABLE_DESTINATIONS,
                        new String[] {
                                COLUMN_DESTINATIONS_ID,
                                COLUMN_DESTINATIONS_NAME_CYR,
                                COLUMN_DESTINATIONS_NAME_LAT
                        }, null, null, null, null, null);
        while (cursor.moveToNext()) {
            int ID = cursor.getInt(0);
            String name_cyr = cursor.getString(1);
            String name_lat = cursor.getString(2);
            DestinationPoint destinationPoint = new DestinationPoint(ID, name_cyr, name_lat);
            // Be careful, IDs in DB start from 1, while we will access destinationPoints
            // by array index that start from 0
            destinationPoints[ID] = destinationPoint;
        }
        cursor.close();
    }

    private void loadReverseRoutes() {
        int count = getCount(TABLE_REVERSEROUTES);
        reverseRoutes = new ReverseRoute[count];
        Cursor cursor = mDataBase.query(TABLE_REVERSEROUTES,
                        new String[] {
                                COLUMN_REVERSEROUTES_DESTIONATIONID,
                                COLUMN_REVERSEROUTES_ROUTEIDS
                        }, null, null, null, null, null);
        while (cursor.moveToNext()) {
            int destinationID = cursor.getInt(0);
            String routeIds = cursor.getString(1);
            ReverseRoute reverseRoute = new ReverseRoute(destinationID, routeIds);
            reverseRoutes[destinationID] = reverseRoute;
        }
        cursor.close();
    }

    private void loadReachableDestinations() {
        int count = getCount(TABLE_REACHABLEDESTINATIONS);
        reachableDestinations = new ReachableDestinations[count];
        Cursor cursor = mDataBase.query(TABLE_REACHABLEDESTINATIONS,
                new String[]{
                        COLUMN_REACHABLEDESTINATIONS_DESTINATIONID,
                        COLUMN_REACHABLEDESTINATIONS_REACHABLEDESTINATIONIDS
                }, null, null, null, null, null);
        while (cursor.moveToNext()) {
            int destinationID = cursor.getInt(0);
            String reachableDestinationIds = cursor.getString(1);
            ReachableDestinations reachableDestination = new ReachableDestinations(destinationID, reachableDestinationIds);
            reachableDestinations[destinationID] = reachableDestination;
        }
        cursor.close();

    }

    private int getCount(String tableName) {
        Cursor countCursor = mDataBase.rawQuery("select count(*) from " + tableName, null);
        countCursor.moveToFirst();
        int count = countCursor.getInt(0);
        countCursor.close();
        return count;
    }
}
