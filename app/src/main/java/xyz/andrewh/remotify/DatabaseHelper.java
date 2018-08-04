package xyz.andrewh.remotify;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import static android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "application_preferences.db";
    private static final int DATABASE_VERSION = 3;
    public static final String TABLE_NAME = "application_preferences";
    public static final String COLUMN_ID = "_id";
    public final static String COLUMN_NAME = "name";
    public final static String COLUMN_PACKAGE_NAME = "package_name";
    public static final String COLUMN_MODIFIED_TIME = "modified_time";
    public static final String COLUMN_CREATED_TIME = "created_time";

    private static final String CREATE_TABLE = "create table "
            + TABLE_NAME
            + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_NAME + " text not null, "
            + COLUMN_PACKAGE_NAME + " text not null " +
            ")";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void save(ApplicationModel app){
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put(COLUMN_PACKAGE_NAME, app.getPackageName());
        values.put(COLUMN_NAME, app.getName());

        db.insertWithOnConflict(TABLE_NAME, null, values, CONFLICT_REPLACE);
        db.close();
    }

    public ArrayList<ApplicationModel> findAll(){
        ArrayList<ApplicationModel> appList =new ArrayList<ApplicationModel>();
        String query="SELECT * FROM "+TABLE_NAME;

        SQLiteDatabase db=this.getReadableDatabase();
        Cursor cursor=db.rawQuery(query, null);

        if(cursor.moveToFirst()){
            do{
                ApplicationModel app=new ApplicationModel();
                app.setName(cursor.getString(1));
                app.setChecked(true);
                appList.add(app);
            }while(cursor.moveToNext());
        }

        return appList;
    }

    public ArrayList<String> findAllPackages(){
        ArrayList<String> packageList =new ArrayList<String>();
        String query="SELECT * FROM "+TABLE_NAME;

        SQLiteDatabase db=this.getReadableDatabase();
        Cursor cursor=db.rawQuery(query, null);

        if(cursor.moveToFirst()){
            do{
                packageList.add(cursor.getString(2));
            }while(cursor.moveToNext());
        }

        return packageList;
    }

    public void delete(ApplicationModel app){
        SQLiteDatabase db=this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_PACKAGE_NAME +"=?", new String[]{String.valueOf(app.getName())});
        db.close();
    }

    public void clearTable(){
        SQLiteDatabase db=this.getWritableDatabase();
        db.execSQL("DROP TABLE "+TABLE_NAME);
        onCreate(db);
    }
}