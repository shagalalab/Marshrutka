package com.shagalalab.marshrutka.db;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.shagalalab.marshrutka.data.DestinationPoint;
import com.shagalalab.marshrutka.data.ReverseRoute;
import com.shagalalab.marshrutka.data.Route;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DbHelper extends SQLiteOpenHelper {

    public static final String TABLE_ROUTES = "routes";
    public static final String COLUMN_ROUTES_ID = "_id";
    public static final String COLUMN_ROUTES_TYPE = "type";
    public static final String COLUMN_ROUTES_DISPLAYNO = "displaynumber";
    public static final String COLUMN_ROUTES_POINTA = "pointA";
    public static final String COLUMN_ROUTES_POINTB = "pointB";
    public static final String COLUMN_ROUTES_POINTC = "pointC";

    public static final String TABLE_DESTINATIONS = "destinations";
    public static final String COLUMN_DESTINATIONS_ID = "_id";
    public static final String COLUMN_DESTINATIONS_NAME = "name";

    public static final String TABLE_REVERSEROUTES = "reverseroutes";
    public static final String COLUMN_REVERSEROUTES_ID = "_id";
    public static final String COLUMN_REVERSEROUTES_DESTIONATIONID = "destinationId";
    public static final String COLUMN_REVERSEROUTES_ROUTEIDS = "routeIds";

    public DestinationPoint[] destinationPoints;
    public ReverseRoute[] reverseRoutes;
    public Route[] routes;

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
    public DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.mContext = context;
        DB_PATH = String.format("/data/data/%s/databases/", context.getPackageName());
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
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    // Add your public helper methods to access and get content from the database.
    // You could return cursors by doing "return mDataBase.query(....)" so it'd be easy
    // to you to create adapters for your views.

    public void loadAllData() {
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
            route.pointA = destinationPoints[cursor.getInt(3)-1];
            route.pointB = destinationPoints[cursor.getInt(4)-1];
            int pointC = cursor.getInt(5);
            if (pointC != -1) {
                route.pointC = destinationPoints[cursor.getInt(5) - 1];
            }

            routes[route.ID-1] = route;
        }
        cursor.close();
    }

    private void loadDestinationPoints() {
        int count = getCount(TABLE_DESTINATIONS);
        destinationPoints = new DestinationPoint[count];
        Cursor cursor = mDataBase.query(TABLE_DESTINATIONS,
                        new String[] {
                                COLUMN_DESTINATIONS_ID,
                                COLUMN_DESTINATIONS_NAME
                        }, null, null, null, null, null);
        while (cursor.moveToNext()) {
            int ID = cursor.getInt(0);
            String name = cursor.getString(1);
            DestinationPoint destinationPoint = new DestinationPoint(ID, name);
            // Be careful, IDs in DB start from 1, while we will access destinationPoints
            // by array index that start from 0
            destinationPoints[ID-1] = destinationPoint;
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
            reverseRoutes[destinationID-1] = reverseRoute;
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
