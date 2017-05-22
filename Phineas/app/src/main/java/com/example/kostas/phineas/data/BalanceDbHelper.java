package com.example.kostas.phineas.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.kostas.phineas.data.BalanceContract.BalanceEntry;

/**
 * Created by Kostas on 8/5/2017.
 */

public class BalanceDbHelper extends SQLiteOpenHelper{

    private static final String DATABASE_NAME = "balances.db";

    private static final int DATABASE_VERSION = 1;

    public BalanceDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_BALANCE_TABLE = "CREATE TABLE " +
                BalanceEntry.TABLE_NAME       + " (" +
                BalanceEntry._ID              + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                BalanceEntry.COLUMN_NAME      + " TEXT NOT NULL, " +
                BalanceEntry.COLUMN_AMOUNT    + " REAL NOT NULL);";
        sqLiteDatabase.execSQL(SQL_CREATE_BALANCE_TABLE);
        ContentValues cv = new ContentValues();
        cv.put(BalanceEntry.COLUMN_NAME, "Account");
        cv.put(BalanceEntry.COLUMN_AMOUNT, 750);
        sqLiteDatabase.insert(BalanceEntry.TABLE_NAME,null, cv);
        cv.put(BalanceEntry.COLUMN_NAME, "Wallet");
        cv.put(BalanceEntry.COLUMN_AMOUNT, 50);
        sqLiteDatabase.insert(BalanceEntry.TABLE_NAME,null, cv);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + BalanceEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    /**
     * Updates the specified column in balances.db with the new value
     *
     * @param newValue The new value of the balance column
     * @param column The column name, which will have it's balance changed.
     * @param mBalanceDb The Db that will be updated.
     */
    public static void updateBalanceDb(double newValue, String column, SQLiteDatabase mBalanceDb){
        String SQL_UPDATE_BALANCE = "UPDATE " +
                BalanceEntry.TABLE_NAME + " SET " +
                BalanceEntry.COLUMN_AMOUNT + " = " +
                String.valueOf(newValue) + " WHERE " +
                BalanceEntry.COLUMN_NAME + " = '"+column+"'";
        mBalanceDb.execSQL(SQL_UPDATE_BALANCE);

    }

    /**
     * Query the database to return a balance (wallet, account etc)
     * @param mBalanceDb the balance database to be queried
     * @param name The name of the balance that will be returned (e.g. wallet, account)
     *
     * @return a cursor containing the results of the query
     */
    public static Cursor getBalance(SQLiteDatabase mBalanceDb, String name){
        return mBalanceDb.query(BalanceEntry.TABLE_NAME,
                null,
                BalanceEntry.COLUMN_NAME + " = '"+name+"'",
                null,
                null,
                null,
                null);
    }
}
