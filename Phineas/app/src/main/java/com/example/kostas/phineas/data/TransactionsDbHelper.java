package com.example.kostas.phineas.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.kostas.phineas.Utilities.FormatUtils;
import com.example.kostas.phineas.data.TransactionsContract.TransactionEntry;

/**
 * Created by Kostas on 5/5/2017.
 */

public class TransactionsDbHelper extends SQLiteOpenHelper{

    private static final String DATABASE_NAME = "transactions.db";

    private static final int DATABASE_VERSION = 2;

    public TransactionsDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_TRANSACTIONS_TABLE  = "CREATE TABLE " +
                TransactionEntry.TABLE_NAME         + " (" +
                TransactionEntry._ID                + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TransactionEntry.COLUMN_NAME        + " TEXT, " +
                TransactionEntry.COLUMN_OPERATOR    + " TEXT NOT NULL, " +
                TransactionEntry.COLUMN_AMOUNT      + " REAL NOT NULL, " +
                TransactionEntry.COLUMN_CATEGORY    + " TEXT, " +
                TransactionEntry.COLUMN_DESCRIPTION + " TEXT, "+
                TransactionEntry.COLUMN_DATETIME    + " DATETIME NOT NULL);";
        sqLiteDatabase.execSQL(SQL_CREATE_TRANSACTIONS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TransactionEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public static long addNewTransactionToDb(String name, String operator, double amount, String category,
                                             String description, String datetime, SQLiteDatabase mDb){
        ContentValues contentValues = new ContentValues();

        contentValues.put(TransactionsContract.TransactionEntry.COLUMN_NAME,name);
        contentValues.put(TransactionsContract.TransactionEntry.COLUMN_OPERATOR,operator);
        contentValues.put(TransactionsContract.TransactionEntry.COLUMN_AMOUNT, amount);
        contentValues.put(TransactionsContract.TransactionEntry.COLUMN_CATEGORY, category);
        contentValues.put(TransactionsContract.TransactionEntry.COLUMN_DESCRIPTION, description);

        // Whenever datetime is null, we want SQL to assign the Current time as a default value.
        // That's why we don't send the null.
        if (datetime!=null){
            contentValues.put(TransactionsContract.TransactionEntry.COLUMN_DATETIME, datetime);
        }else{
            datetime = FormatUtils.getCurrentDateTime(null,null);
            contentValues.put(TransactionsContract.TransactionEntry.COLUMN_DATETIME, datetime);
        }

        return mDb.insert(TransactionsContract.TransactionEntry.TABLE_NAME, null, contentValues);
    }

    public static int updateTransactionInDb(long id, String name, String operator, double amount,
                                            String category, String description, String datetime,
                                            SQLiteDatabase mDb){
        ContentValues contentValues = new ContentValues();

        contentValues.put(TransactionsContract.TransactionEntry.COLUMN_NAME,name);
        contentValues.put(TransactionsContract.TransactionEntry.COLUMN_OPERATOR,operator);
        contentValues.put(TransactionsContract.TransactionEntry.COLUMN_AMOUNT, amount);
        contentValues.put(TransactionsContract.TransactionEntry.COLUMN_CATEGORY, category);
        contentValues.put(TransactionsContract.TransactionEntry.COLUMN_DESCRIPTION, description);

        // Whenever datetime is null, we want SQL to assign the Current time as a default value.
        // That's why we don't send the null.
        if (datetime!=null){
            contentValues.put(TransactionsContract.TransactionEntry.COLUMN_DATETIME, datetime);
        }

        return mDb.update(TransactionsContract.TransactionEntry.TABLE_NAME, contentValues,
                TransactionsContract.TransactionEntry._ID+"="+id, null);
    }

    public static void clearTransactionsDatabase(SQLiteDatabase db){
        db.execSQL("DROP TABLE IF EXISTS " + TransactionEntry.TABLE_NAME);
        final String SQL_CREATE_TRANSACTIONS_TABLE  = "CREATE TABLE " +
                TransactionEntry.TABLE_NAME         + " (" +
                TransactionEntry._ID                + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TransactionEntry.COLUMN_NAME        + " TEXT, " +
                TransactionEntry.COLUMN_OPERATOR    + " TEXT NOT NULL, " +
                TransactionEntry.COLUMN_AMOUNT      + " REAL NOT NULL, " +
                TransactionEntry.COLUMN_CATEGORY    + " TEXT, " +
                TransactionEntry.COLUMN_DESCRIPTION + " TEXT, "+
                TransactionEntry.COLUMN_DATETIME    + " DATETIME NOT NULL);";
        db.execSQL(SQL_CREATE_TRANSACTIONS_TABLE);
    }

    public static boolean removeTransactionFromDatabase(SQLiteDatabase db, long id){
        String SQL_WHERE_CLAUSE = TransactionEntry._ID + "=" + id;
        return db.delete(TransactionEntry.TABLE_NAME,SQL_WHERE_CLAUSE,null) > 0;
    }

    public static Cursor getTransaction(SQLiteDatabase db, long id){
        String SQL_WHERE_CLAUSE = TransactionEntry._ID + "=" + id;
        return db.query(TransactionEntry.TABLE_NAME,null,SQL_WHERE_CLAUSE,null,null,null,null);
    }


    /**
     * Query the database to get all the transactions. Transactions are ordered by descending
     * datetime.
     *
     * @return Cursor that has the results of the query, orderded by descending datetime.
     */
    public static Cursor getAllTransactions(SQLiteDatabase db){
        return db.query(TransactionEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                TransactionEntry.COLUMN_DATETIME+" DESC");
    }


    /**
     * Query the database to get the sum of the expenses for each day
     *
     * @param db: The database we apply the query to.
     * @return the Cursor containing a column with the dates of each day and a column with the total
     *         amount spent that day. The cursor results are ordered by descending date.
     */
    public static Cursor getTotalExpensesPerDay(SQLiteDatabase db){
        String SQL_QUERY =
                "SELECT sum("+ TransactionEntry.COLUMN_AMOUNT+") as "+TransactionEntry.COLUMN_SUM_TOTAL+"," +
                " DATE("+TransactionEntry.COLUMN_DATETIME+") as "+TransactionEntry.COLUMN_DATE +
                " FROM "+ TransactionEntry.TABLE_NAME +
                " GROUP BY DATE("+TransactionEntry.COLUMN_DATETIME+") " +
                " ORDER BY DATE("+TransactionEntry.COLUMN_DATETIME+") DESC;";
        return db.rawQuery(SQL_QUERY, null);
    }
}
