package com.shagalalab.marshrutka.db;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.shagalalab.marshrutka.App;
import com.shagalalab.marshrutka.Utils;
import com.shagalalab.marshrutka.data.DestinationPoint;
import com.shagalalab.marshrutka.data.ReachableDestinations;
import com.shagalalab.marshrutka.data.ReverseRoute;
import com.shagalalab.marshrutka.data.Route;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class DbHelper extends SQLiteOpenHelper {
    private static final String TAG = "marshrutka";

    private static final String TABLE_ROUTES = "routes";
    private static final String COLUMN_ROUTES_ID = "id";
    private static final String COLUMN_ROUTES_TYPE = "type";
    private static final String COLUMN_ROUTES_DISPLAYNO = "displaynumber";
    private static final String COLUMN_ROUTES_DESCRIPTION_CYR = "description_cyr";
    private static final String COLUMN_ROUTES_DESCRIPTION_LAT = "description_lat";
    private static final String COLUMN_ROUTES_PATHPOINTIDS = "pathPointIds";

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

    public HashMap<String, Integer> destinationToIdMapping = new HashMap<>();

    //The Android's default system path of your application database.
    private final String DB_PATH;

    private static final String DB_NAME = "marshrutka.db";
    private static final int DB_VERSION = 2;

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

    public static void reset() {
        _instance = null;
    }

    /**
     * Creates a empty database on the system and rewrites it with your own database.
     */
    public void createDataBaseIfNotExists() throws IOException {

        boolean dbExist = checkDataBase();

        if (dbExist) {
            //do nothing - database already exist
            Log.d(TAG, "DbHelper.createDatabaseIfNotExists() db exists & version is correct -> no need to copy from assets");
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

    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     *
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase() {
        SQLiteDatabase checkDB = null;

        try {
            String myPath = DB_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
        } catch (SQLiteException e) {
            //database doesn't exist yet.
        }

        if (checkDB != null) {
            checkDB.close();
            int currentDbVersion = Utils.getDbVersion(mContext);
            if (currentDbVersion != DB_VERSION) {
                Log.d(TAG, "DbHelper.checkDatabase() currentDb.version=" + currentDbVersion + ", newVersion=" + DB_VERSION);
                return false;
            }
        }

        return checkDB != null ? true : false;
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

        Utils.setDbVersion(mContext, DB_VERSION);

        Log.d(TAG, "DbHelper.copyDatabase()");
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
                            COLUMN_ROUTES_DESCRIPTION_CYR,
                            COLUMN_ROUTES_DESCRIPTION_LAT,
                            COLUMN_ROUTES_PATHPOINTIDS
                        }, null, null, null, null, null);

        while (cursor.moveToNext()) {
            Route route = new Route();
            route.ID = cursor.getInt(0);
            route.isBus = cursor.getInt(1) == 1;
            route.displayNo = cursor.getInt(2);
            route.descriptionCyr = cursor.getString(3);
            route.descriptionLat = cursor.getString(4);
            String[] pathPointIds = cursor.getString(5).trim().split(",");
            int len = pathPointIds.length;
            ArrayList<DestinationPoint> destPoints = new ArrayList<>();
            Stack<Integer> destPointStack = new Stack<>();
            for (int i=0; i<len; i++) {
                Integer destinationPointId = Integer.parseInt(pathPointIds[i]);
                destPoints.add(destinationPoints[destinationPointId]);
                if (i == len - 1) {
                    if (destPointStack.contains(destinationPointId)) {
                        while (!destPointStack.pop().equals(destinationPointId));
                    } else {
                        destPointStack.clear();
                    }
                } else {
                    destPointStack.push(destinationPointId);
                }
            }
            while (!destPointStack.isEmpty()) {
                int current = destPointStack.pop();
                destPoints.add(destinationPoints[current]);
            }

            route.pathPoints = destPoints;
            routes[route.ID] = route;
        }
        cursor.close();
    }

    private void loadDestinationPoints() {

        boolean isInterfaceCyrillic = ((App)mContext.getApplicationContext()).isCurrentLocaleCyrillic();

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
            DestinationPoint destinationPoint = new DestinationPoint(isInterfaceCyrillic, ID,
                    isInterfaceCyrillic ? name_cyr : name_lat);
            destinationPoints[ID] = destinationPoint;
            destinationToIdMapping.put(name_cyr, ID);
            destinationToIdMapping.put(name_lat, ID);
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
