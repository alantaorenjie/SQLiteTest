package com.trj.sqlite.sqlitesample;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

/**
 * @author TRJ
 * @date 2018/11/26
 * Description:
 */
public class DbHelper extends SQLiteOpenHelper {

    public static final String TAG = "DbHelper";

    private static String DEFAULT_DB_NAME = "test.db";
    private static int DEFAULT_VERSION = 1;

    private final Context mContext;
    private final String mName;
    private final SQLiteDatabase.CursorFactory mFactory;
    private final int mNewVersion;

    public DbHelper(Context context) {
        this(context, DEFAULT_DB_NAME, null, DEFAULT_VERSION);
    }

    public DbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context,name,factory,version);
        if (version < 1) throw new IllegalArgumentException("Version must be >= 1, was " + version);
        mContext = context;
        mName = name;
        mFactory = factory;
        mNewVersion = version;
        getWritableDatabase().enableWriteAheadLogging();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void insert(String dec){
        log("insert");
        ContentValues cv = new ContentValues();
        cv.put(DEC, dec);
        getWritableDatabase().insert(TABLE_NAME, null, cv);
    }

    public int getCount() {
        return getCount(getReadableDatabase());
    }


    public int getCount(SQLiteDatabase db) {
        int count = -1;
        if(db == null)	db = getReadableDatabase();
        Cursor c = db.query(TABLE_NAME, null, null, null, null, null, null);
        try{
            if (c.moveToFirst()) {
                count = c.getCount();
            }
        }finally{
            c.close();
        }
        log( "getCount="+count);
        return count;
    }

    public static final String TABLE_NAME = "test_t";
    public static final String ID = "_id";
    public static final String DEC = "descreption";

    private static final String CREATE_TABLE = "create table if not exists "+TABLE_NAME +" ( "
            +ID + " INTEGER primary key autoincrement, " +DEC + " text " +" );";


    void log(String dec){
        Log.i(TAG, dec + ". current thread="+Thread.currentThread().getName());
    }


    public SQLiteDatabase getOnlyReadDatabase() {
        try{
            getWritableDatabase(); //保证数据库版本最新
        }catch(SQLiteException e){
            Log.e(TAG, "Couldn't open " + mName + " for writing (will try read-only):",e);
        }

        SQLiteDatabase db = null;
        try {
            String path = mContext.getDatabasePath(mName).getPath();
            db = SQLiteDatabase.openDatabase(path, mFactory, SQLiteDatabase.OPEN_READONLY);
            if (db.getVersion() != mNewVersion) {
                throw new SQLiteException("Can't upgrade read-only database from version " +
                        db.getVersion() + " to " + mNewVersion + ": " + path);
            }

            onOpen(db);
            readOnlyDbs.add(db);
            return db;
        } finally {
        }
    }


    private List<SQLiteDatabase> readOnlyDbs = new LinkedList<SQLiteDatabase>();

    @Override
    public synchronized void close() {
        super.close();
        for(SQLiteDatabase db : readOnlyDbs){
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        readOnlyDbs.clear();
    }

}
