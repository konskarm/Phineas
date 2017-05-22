package com.example.kostas.phineas;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Paint;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.kostas.phineas.data.BalanceContract;
import com.example.kostas.phineas.data.BalanceDbHelper;
import com.example.kostas.phineas.data.TransactionsContract.TransactionEntry;
import com.example.kostas.phineas.data.TransactionsDbHelper;
import com.example.kostas.phineas.Utilities.FormatUtils;

import java.util.Calendar;

import static java.lang.Math.round;

public class TransactionDetailsActivity extends AppCompatActivity {

    private static final boolean EDIT_MODE = true;
    private static final boolean SAVE_MODE = false;
    private static final String PLUS_BUTTON = "+";
    private static final String MINUS_BUTTON = "-";
    private TextView mTransactionDetailsTextView;
    private EditText mReasonEditText;
    private EditText mAmountEditText;
    private EditText mCategoryEditText;
    private TextView mTimeEditText;
    private TextView mDateEditText;
    private EditText mDescriptionEditText;
    private Button mPlusButton;
    private Button mMinusButton;
    private Toast mToast;


    private String datetime;
    private String time;
    private String name;
    private String operator;
    private double amount;
    private double oldAmount;
    private String category;
    private String description;
    private long id=-1;
    private boolean isInEditMode;
    private double mWalletValue = 50;


    private SQLiteDatabase mDb;
    private SQLiteDatabase mBalanceDb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_details);

        // Reference the views of this activity
        mTransactionDetailsTextView = (TextView) findViewById(R.id.tv_transaction_details);
        mReasonEditText = (EditText) findViewById(R.id.et_transaction_details_reason);
        mAmountEditText = (EditText) findViewById(R.id.et_transaction_details_amount);
        mCategoryEditText = (EditText) findViewById(R.id.et_transaction_details_category);
        mTimeEditText = (TextView) findViewById(R.id.et_transaction_details_time);
        mDateEditText = (TextView) findViewById(R.id.et_transaction_details_date);
        mDescriptionEditText = (EditText) findViewById(R.id.et_transaction_details_description);
        mPlusButton = (Button) findViewById(R.id.button_details_plus);
        mMinusButton = (Button) findViewById(R.id.button_details_minus);
        mToast = new Toast(this);

        // Set time and date text fields to be underlined
        mTimeEditText.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
        mDateEditText.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);

        // Show the back button next to the application name
        ActionBar actionBar = this.getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Get extras from the intent that was used to call this activity
        Bundle extras = getIntent().getExtras();
        getValuesFromIntentExtras(extras);

        /** Initialize the SQLiteDatabases and retrieve all the transactions and balances */
        TransactionsDbHelper mDbHelper = new TransactionsDbHelper(this);
        mDb = mDbHelper.getWritableDatabase();

        BalanceDbHelper mBalanceDbHelper = new BalanceDbHelper(this);
        mBalanceDb = mBalanceDbHelper.getWritableDatabase();
        getWalletBalance();


    }

    /**
     * Function that gets called in onCreate to get all the extra values from the intent, to display
     * them inside the Transaction Details
     * @param extras the bundle containing all the extras
     */
    private void getValuesFromIntentExtras(Bundle extras){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String dateKey = getResources().getString(R.string.pref_date_format_key);
        String dateDefault = getResources().getString(R.string.pref_date_format_default_value);
        String dateFormat = sharedPreferences.getString(dateKey,dateDefault);

        String timeKey = getResources().getString(R.string.pref_time_format_key);
        String timeDefault = getResources().getString(R.string.pref_time_format_default_value);
        String timeFormat = sharedPreferences.getString(timeKey, timeDefault);

        if (extras == null){
            mTransactionDetailsTextView.setText("No extras in the intent");
        } else {
            if (extras.containsKey(MainActivity.MAKE_DETAIL_FIELDS_EDITABLE)){
                boolean editable = extras.getBoolean(MainActivity.MAKE_DETAIL_FIELDS_EDITABLE);
                isInEditMode = editable;
                invalidateOptionsMenu();
                setAllFieldsEditability(editable);
            }
            if (extras.containsKey(TransactionEntry._ID)){
                // Keep the id in case we need to alter any fields on the specific transaction
                id = extras.getLong(TransactionEntry._ID);
            }
            if (extras.containsKey(TransactionEntry.COLUMN_DATETIME)){
                // Get the datetime and split it to date and time fields to display in the appropriate
                // views
                datetime = extras.getString(TransactionEntry.COLUMN_DATETIME);
                String[] split = FormatUtils.splitDateTime(datetime, FormatUtils.DEFAULT_SQL_DATETIME_FORMAT,
                        dateFormat, timeFormat);
                mDateEditText.setText(split[0]);
                mTimeEditText.setText(split[1]);
            }
            if (extras.containsKey(TransactionEntry.COLUMN_OPERATOR)){
                // Get the operator (+ or -) and use it to toggle the Buttons
                operator = extras.getString(TransactionEntry.COLUMN_OPERATOR);
                toggleButton(operator);
            }
            if (extras.containsKey(TransactionEntry.COLUMN_AMOUNT)){
                // Get the old amount of the transaction (if it is an already registered transaction).
                oldAmount = Double.parseDouble(extras.getString(TransactionEntry.COLUMN_AMOUNT));
                mAmountEditText.setText(String.valueOf(Math.abs(oldAmount)));
                mAmountEditText.setHint("");
            }
            if (extras.containsKey(TransactionEntry.COLUMN_NAME)){
                // Get the name(reason) of the specific transaction.
                name = extras.getString(TransactionEntry.COLUMN_NAME);
                if (name == null)
                    name = "";
                mReasonEditText.setText(name);
                mReasonEditText.setHint("");
            }
            if (extras.containsKey(TransactionEntry.COLUMN_CATEGORY)){
                // Get the category of the specific transaction.
                category = extras.getString(TransactionEntry.COLUMN_CATEGORY);
                if (category==null)
                    category="";
                mCategoryEditText.setText(category);
                mCategoryEditText.setHint("");
            }
            if (extras.containsKey(TransactionEntry.COLUMN_DESCRIPTION)){
                // Get the description of the specific transaction.
                description = extras.getString(TransactionEntry.COLUMN_DESCRIPTION);
                if (description == null)
                    description = "";
                mDescriptionEditText.setText(description);
                mDescriptionEditText.setHint("");
            }
        }
    }


    /**
     * Whenever the time TextView is clicked, this function gets called to display a TimePicker dialog
     * @param v
     */
    public void onTimeClicked(View v){

        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final String timeFormat = sharedPreferences.getString(getString(R.string.pref_time_format_key),
                getString(R.string.pref_time_format_default_value));

        TimePickerDialog timePickerDialog = new TimePickerDialog(TransactionDetailsActivity.this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,
                                          int minute) {
                        String timeSelected = "";
                        if (hourOfDay < 10){
                            timeSelected += "0";
                        }
                        timeSelected += String.valueOf(hourOfDay)+":";
                        if (minute < 10) {
                            timeSelected += "0";
                        }
                        timeSelected += String.valueOf(minute);
                        timeSelected += ":00";
                        timeSelected = FormatUtils.switchTimeFormat(timeSelected, FormatUtils.DEFAULT_TIME_FORMAT,
                                timeFormat);
                        mTimeEditText.setText(timeSelected);
                    }
                }, currentHour, currentMinute, true);
        timePickerDialog.show();
    }

    /**
     * Whenever the time TextView is clicked, this function gets called to display a DatePicker dialog.
     * The result is to display the date picked on the associated EditText view, based on the date format
     * preferred by the user.
     * @param v
     */
    public void onDateClicked(View v){

        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final String dateFormat = sharedPreferences.getString(getString(R.string.pref_date_format_key),
                getString(R.string.pref_date_format_default_value));

        DatePickerDialog datePickerDialog = new DatePickerDialog(TransactionDetailsActivity.this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                        String date = FormatUtils.switchDateFormat(dayOfMonth + "/" + (month + 1) + "/" + year,
                                FormatUtils.DEFAULT_DATE_FORMAT, dateFormat);
                        mDateEditText.setText(date);
                    }

                }, currentYear, currentMonth, currentDay);
        datePickerDialog.show();
    }


    /**
     * Set the editability of the views.
     * @param editable Boolean parameter. If true, vies will be editable, else they won't be.
     */
    private void setAllFieldsEditability(boolean editable){
        setEditability(mReasonEditText, editable);
        setEditability(mAmountEditText, editable);
        setEditability(mCategoryEditText, editable);
        mDateEditText.setClickable(editable);
        mTimeEditText.setClickable(editable);
        mMinusButton.setClickable(editable);
        mPlusButton.setClickable(editable);
        setEditability(mDescriptionEditText, editable);
    }

    /**
     * Set the editability of a specific edittext
     * @param et The editText view that will be set as editable
     * @param editable Boolean parameter. If true, editText will be editable, else it won't be
     */
    private void setEditability(EditText et, boolean editable){
        et.setFocusable(editable);
        et.setFocusableInTouchMode(editable);
        et.setCursorVisible(editable);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.details_activity_menu, menu);
        MenuItem mEditItem = menu.findItem(R.id.menu_item_edit);
        MenuItem mSaveItem = menu.findItem(R.id.menu_item_save);
        if(isInEditMode){
            mEditItem.setVisible(false);
            mSaveItem.setVisible(true);
        }
        else {
            mEditItem.setVisible(true);
            mSaveItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuItemThatWasSelected = item.getItemId();

        switch(menuItemThatWasSelected){
            case android.R.id.home: //Home button has been pressed --> return to MainActivity
                NavUtils.navigateUpFromSameTask(this);
                break;

            case R.id.menu_item_edit://Edit button has been pressed --> Make fields editable

                // We change to Edit mode, which means we open the fields to be editable
                changeMode(EDIT_MODE);
                mReasonEditText.requestFocus();
                break;

            case R.id.menu_item_save: //Save button has been pressed --> save values and make fields uneditable

                // Get the values from the views and if this function returns false, it means that
                // AmountEditText was empty --> return false;
                if(!getValuesFromViews())
                    return false;

                // Change the wallet balance based on the new amount
                if(!changeWalletBalanceOnSave()){
                    return false;
                }

                // Change to SAVE mode, which means that we remove the editability of the views.
                changeMode(SAVE_MODE);

                // Hide keyboard after pressing the save button
                hideKeyboard();
                break;
        }
        return true;
    }

    /**
     * When save button is hit, save the transaction. If it was a new transaction, create a new entry
     * on the Transactions db, or else modify the existing transaction based on its id. Afterwards,
     * we also change the wallet balance in balances.db
     *
     * @return true if transactions were successful or false if there was a problem
     * (e.g. the wallet balance was not sufficient to complete the transaction)
     */
    public boolean changeWalletBalanceOnSave(){
        // If id == -1 it means that this is a new transaction, so we add the amount to the
        // walletValue and then we add the new Transaction in the transactions.db
        if (id == -1) {
            if(mWalletValue + amount >=0){
                mWalletValue += amount;
                // We store the id that was automatically generated in the database into id.
                id = TransactionsDbHelper.addNewTransactionToDb(name,operator,amount,category,description,datetime,mDb);
            } else {
                showToast("Wallet balance is " + mWalletValue + " and is not sufficient for this transaction.");
                return false;
            }
        } else{
            if (mWalletValue - oldAmount + amount >= 0){
                // if instead id has a positive value, it means that this is not a new transaction.
                // We have to update the wallet value so as to remove the old amount and add the
                // new amount after the save button has been pressed. then we update the
                // transactions db to change the columns in the specific transaction
                mWalletValue = mWalletValue - oldAmount + amount;
                int t = TransactionsDbHelper.updateTransactionInDb(id,name,operator,amount,category,description,datetime,mDb );
                oldAmount = amount;
                showToast("Transaction was successfully saved");
            }else {
                oldAmount = amount;
                showToast("Wallet balance is " + mWalletValue + " and is not sufficient for this transaction.");
                return false;
            }
        }

        // Update the Wallet balance with the new value
        BalanceDbHelper.updateBalanceDb(mWalletValue,"Wallet",mBalanceDb);
        showToast("Transaction was successfully saved");

        return true;
    }

    public void hideKeyboard(){
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        try {
            imm.hideSoftInputFromWindow(mAmountEditText.getWindowToken(), 0);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get all the values from the fields in the Transaction Details Activity and store them in
     * the appropriate variables.
     *
     * @return true if value retrieval was successful or false if the Amount field was left empty
     */
    public boolean getValuesFromViews(){

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Get the name(reason) field
        name = mReasonEditText.getText().toString();

        // Get the amount field. If it is empty, return false.
        if (mAmountEditText.getText().toString().equals("")) {
            Toast.makeText(this, "Amount field should not be empty", Toast.LENGTH_LONG).show();
            return false;
        }
        amount = Double.parseDouble(mAmountEditText.getText().toString());
        if (operator.equals("-")){
            amount = -amount;
        }

        // Get the category field
        category = mCategoryEditText.getText().toString();

        // Get the description field
        description = mDescriptionEditText.getText().toString();

        // Get the date field. If it is left empty, assign the current date based on the preferred date format
        String date = mDateEditText.getText().toString();
        String dateFormat = sharedPreferences.getString(getString(R.string.pref_date_format_key),
                getString(R.string.pref_date_format_default_value));
        if(date.equals("")){
            date = FormatUtils.getCurrentDate(dateFormat);
            mDateEditText.setText(date);
        }

        // Get the time field. If it is left empty, assign the current time based on the preferred time format
        String time = mTimeEditText.getText().toString();
        String timeFormat = sharedPreferences.getString(getString(R.string.pref_time_format_key),
                getString(R.string.pref_time_format_default_value));
        if(time.equals("")){
            time = FormatUtils.getCurrentTime(timeFormat);
            mTimeEditText.setText(time);
        }

        // Merge the Date and time data into one String variable
        datetime = FormatUtils.mergeDateTime(date, time,
                FormatUtils.DEFAULT_SQL_DATETIME_FORMAT,
                dateFormat + " " + timeFormat);

        // Return true to indicate that value retrieval was successful
        return true;
    }

    /**
     * Toggle between EDIT mode and SAVE mode. EDIT mode has all fields editable, while SAVE mode
     * has all fields uneditable.
     *
     * @param editMode Boolean parameter. If it is true, it means we are in EDIT mode, else we are
     *                 in SAVE mode.
     */
    public void changeMode(boolean editMode){
        // Set global variable isInEditMode
        isInEditMode = editMode;

        // Invalidate Options menu, so that it gets inflated again to properly display edit and save buttons
        invalidateOptionsMenu();

        // Set the editability of the fields based on the selected Mode.
        setAllFieldsEditability(editMode);
    }

    public void onDetailsPlusClicked(View view){
        toggleButton(PLUS_BUTTON);
    }

    public void onDetailsMinusClicked(View view){
        toggleButton(MINUS_BUTTON);
    }


    /**
     * Toggle the + and - buttons to properly select weather the amount will be added or substracted
     *
     * @param button String that is linked to the button that will appear to be selected.
     */
    public void toggleButton(String button){
        if(button.equals(PLUS_BUTTON)){
            mPlusButton.setBackgroundColor(getResources().getColor(R.color.colorPlusButton));
            mMinusButton.setBackgroundColor(getResources().getColor(R.color.colorMinusButtonPressed));
            operator = "+";
        } else if(button.equals(MINUS_BUTTON)){
            mPlusButton.setBackgroundColor(getResources().getColor(R.color.colorPlusButtonPressed));
            mMinusButton.setBackgroundColor(getResources().getColor(R.color.colorMinusButton));
            operator = "-";
        }
    }


    /**
     * Get the wallet balance from the balances database
     */
    public void getWalletBalance(){
        Cursor walletCursor = BalanceDbHelper.getBalance(mBalanceDb,"Wallet");
        if (!walletCursor.moveToFirst())
            return;
        mWalletValue = walletCursor.getDouble(walletCursor.getColumnIndex(BalanceContract.BalanceEntry.COLUMN_AMOUNT));
        mWalletValue = FormatUtils.roundTo2Decimals(mWalletValue);
        walletCursor.close();
    }

    public void showToast(String string){
        mToast.cancel();
        mToast = Toast.makeText(this,
                string,
                Toast.LENGTH_LONG);
        mToast.show();
    }


}
