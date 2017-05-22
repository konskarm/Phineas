package com.example.kostas.phineas;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kostas.phineas.data.BalanceContract.BalanceEntry;
import com.example.kostas.phineas.data.BalanceDbHelper;
import com.example.kostas.phineas.data.TransactionsDbHelper;
import com.example.kostas.phineas.Utilities.FormatUtils;


import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.round;

public class MainActivity extends AppCompatActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener,
        RecentTransactionsFragment.deleteTransactionWalletValueChangeListener{

    private SQLiteDatabase mTransactionsDb;
    private SQLiteDatabase mBalanceDb;
    private EditText mAmountEditText;
    private EditText mReasonEditText;
    private TextView mBankAccountValueTextView;
    private TextView mWalletValueTextView;
    private double mAccountValue = 300;
    private double mWalletValue = 50;
    private double mTransferAmountBetweenBalances = 0;


    ViewPagerAdapter viewPagerAdapter;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    public static final String MAKE_DETAIL_FIELDS_EDITABLE = "make_detail_fields_editable";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /** Initialize views inside the Main Activity UI */
        mAmountEditText = (EditText)findViewById(R.id.et_quick_amount);
        mReasonEditText = (EditText) findViewById(R.id.et_quick_reason);
        mBankAccountValueTextView = (TextView) findViewById(R.id.tv_bank_account_value);
        mWalletValueTextView = (TextView) findViewById(R.id.tv_wallet_value);

        mWalletValueTextView.setText(String.valueOf(mWalletValue));
        mBankAccountValueTextView.setText(String.valueOf(mAccountValue));


        /** Initialize the SQLiteDatabases and retrieve all the transactions and balances */
        TransactionsDbHelper mTransactionsDbHelper = new TransactionsDbHelper(this);
        mTransactionsDb = mTransactionsDbHelper.getWritableDatabase();

        BalanceDbHelper mBalanceDbHelper = new BalanceDbHelper(this);
        mBalanceDb = mBalanceDbHelper.getWritableDatabase();
        getBalances();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        displayAccountBalance(sharedPreferences.getBoolean(getString(R.string.pref_display_account_balance_key),true));

        /** Setup the view pager and tab layout to switch between recent transactions fragment
         * and Calendar fragment */
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.main_activity_tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }


    /**
     * Function to setup the view Pager using a view Pager adapter
     * @param viewPager
     */
    private void setupViewPager(ViewPager viewPager) {
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragment(new RecentTransactionsFragment(), getString(R.string.main_activity_tab_recent_transactions));
        viewPagerAdapter.addFragment(new CalendarFragment(), getString(R.string.main_activity_tab_calendar));
        viewPager.setAdapter(viewPagerAdapter);
    }

    /**
     * ViewPagerAdapter class which is used to handle the fragments displayed in the tab layout
     */
    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }


    /*
       ------------------------
            Button Listeners
       ------------------------
    */

    /**
     * Gets called whenever Plus button of the top layout is clicked.
     * Used to add the amount specified by the amount field, to the wallet.
     *
     * @param view
     */
    public void onPlusButtonClicked(View view){
        if (mAmountEditText.getText().toString().equals(""))
            return;
        addNewTransactionToWallet(mAmountEditText.getText().toString(),"+", mReasonEditText.getText().toString());
    }

    /**
     * Gets called whenever Minus button of the top layout is clicked.
     * Used to subtract the amount specified in the amount field to the wallet.
     * @param view
     */
    public void onMinusButtonClicked(View view){
        if (mAmountEditText.getText().toString().equals(""))
            return;
        addNewTransactionToWallet(mAmountEditText.getText().toString(),"-", mReasonEditText.getText().toString());
    }

    /**
     * Called when ADD DETAILED TRANSACTION button is clicked.
     * Creates an intent and starts the TransactionDetails Activity, passing in true to the
     * MAKE_DETAIL_FIELDS_EDITABLE, which means that all fields will be available to edit when
     * the new activity is created.
     *
     * @param view
     */
    public void onAddDetailedTransactionClicked(View view){
        Intent intent  = new Intent(this, TransactionDetailsActivity.class);
        intent.putExtra(MainActivity.MAKE_DETAIL_FIELDS_EDITABLE, true);
        startActivity(intent);
    }

    /**
     * Called whenever the Transfer Image view is clicked, or if the Transfer Money button of the
     * Options menu is clicked. Opens a dialog which allows the user to specify the amount of money
     * that will be transferred between wallet balance and account balance.
     *
     * @param view
     */
    public void onTransferClicked(View view){
        openTransferMoneyDialog();
    }

    /**
     * Function that inflates a dialog used to transfer money between wallet and account balances
     */
    public void openTransferMoneyDialog(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.transfer_money_dialog, null);

        final EditText mTransferAmountEditText = (EditText) dialogView.findViewById(R.id.et_transfer_money_between_balances_amount);
        final TextView mTransferFromTextView = (TextView) dialogView.findViewById(R.id.tv_transfer_money_from);
        final TextView mTransferToTextView = (TextView) dialogView.findViewById(R.id.tv_transfer_money_to);
        final ImageView mSwitchImageView = (ImageView) dialogView.findViewById(R.id.ib_transfer_money);

        mSwitchImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String temp = mTransferFromTextView.getText().toString();
                mTransferFromTextView.setText(mTransferToTextView.getText().toString());
                mTransferToTextView.setText(temp);
            }
        });

        dialogBuilder.setView(dialogView).setTitle(R.string.transfer_money_between_balances_label);
        dialogBuilder.setPositiveButton(R.string.dialog_button_positive_label, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                mTransferAmountBetweenBalances = Double.parseDouble(mTransferAmountEditText.getText().toString());

                if (mTransferFromTextView.getText().toString().equals(getResources().getString(R.string.wallet))){
                    BalanceDbHelper.updateBalanceDb(mWalletValue-mTransferAmountBetweenBalances,
                            getResources().getString(R.string.wallet),mBalanceDb);

                    BalanceDbHelper.updateBalanceDb(mAccountValue+mTransferAmountBetweenBalances,
                            getResources().getString(R.string.account),mBalanceDb);
                }else{
                    BalanceDbHelper.updateBalanceDb(mWalletValue+mTransferAmountBetweenBalances,
                            getResources().getString(R.string.wallet),mBalanceDb);

                    BalanceDbHelper.updateBalanceDb(mAccountValue-mTransferAmountBetweenBalances,
                            getResources().getString(R.string.account),mBalanceDb);
                }
                getBalances();
                dialog.dismiss();
            }
        })
                .setNegativeButton(R.string.dialog_button_cancel_label, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = dialogBuilder.create();

        alertDialog.show();
    }
    /* --------------------------------------------------------------------------------------- */


    /*
    ------------------------
     Options Menu Functions
    ------------------------
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuItemThatWasSelected = item.getItemId();

        switch (menuItemThatWasSelected){
            case R.id.menu_item_transfer_money:
                openTransferMoneyDialog();
                break;
            case R.id.menu_item_delete_transactions:
//                TransactionsDbHelper.clearTransactionsDatabase(mTransactionsDb);
                swapRecentTransactionsCursor();
                break;
            case R.id.menu_item_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
        }
        return true;
    }
    /* --------------------------------------------------------------------------------------- */



    /*
    ---------------------------
    Shared Preferences Listener
    ---------------------------
     */

    /**
     * Whenever a preference is changed in the settings activity, this function is called to update
     * the UI elements in the main activity that are affected by the changes.
     *
     * @param sharedPreferences
     * @param key
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_display_account_balance_key))) {
            boolean value = sharedPreferences.getBoolean(key, true);
            displayAccountBalance(value);
        }
    }

    /**
     * Sets the visibility of the mBankAccountValueTextView. Called whenever the preference is changed
     * @param value the boolean that controls the visibility
     */
    public void displayAccountBalance(boolean value){
        if (value){
            mBankAccountValueTextView.setVisibility(View.VISIBLE);
        } else {
            mBankAccountValueTextView.setVisibility(View.INVISIBLE);
        }
    }
    /* --------------------------------------------------------------------------------------- */


    /*
    ------------------------------------------
    Rest of the functions used in MainActivity
    ------------------------------------------
     */
    /**
     * Function which is called to initialize the balances in the UI
     */
    private void getBalances (){
        Cursor accountCursor = BalanceDbHelper.getBalance(mBalanceDb, "Account");

        Cursor walletCursor = BalanceDbHelper.getBalance(mBalanceDb, "Wallet");

        if (!accountCursor.moveToFirst())
            return;
        mAccountValue = accountCursor.getDouble(accountCursor.getColumnIndex(BalanceEntry.COLUMN_AMOUNT));
        mBankAccountValueTextView.setText(String.valueOf(round(mAccountValue * 100.0) / 100.0));
        accountCursor.close();

        if (!walletCursor.moveToFirst())
            return;
        mWalletValue = walletCursor.getDouble(walletCursor.getColumnIndex(BalanceEntry.COLUMN_AMOUNT));
        mWalletValueTextView.setText(String.valueOf(round(mWalletValue * 100.0) / 100.0));
        walletCursor.close();

    }

    /**
     * Function that get called whenever a new transaction needs to be added to the wallet balance
     *
     * @param amountString The amount that will be added or substracted tot he wallet balance
     * @param operator The operator of the transaction (+ or -)
     * @param reason The reason of the quick transaction
     */
    public void addNewTransactionToWallet(String amountString, String operator, String reason){
        double amount = Double.parseDouble(amountString);
        double roundedAmount = FormatUtils.roundTo2Decimals(amount);

        if (operator.equals("-"))
            roundedAmount = -roundedAmount;

        // Add the new amount to the wallet balance. Display a toast error message if the transaction
        // was not successful.
        if(!updateWalletValue(roundedAmount)){
            Toast.makeText(this, "Wallet balance not sufficient for this transaction", Toast.LENGTH_LONG).show();
            return;
        }

        // Add the transaction to the transactions database
        TransactionsDbHelper.addNewTransactionToDb(reason, operator, roundedAmount, null, null, null, mTransactionsDb);

        // Swap the Cursor of the Recent Transactions Adapter to reload all the transactions
        swapRecentTransactionsCursor();

        //Swap the cursor of the Calendar Adapter to reload the data in Calendar Fragment
        swapCalendarCursor();

        // Clear the edit text and the focus from it, after pressing a button
        mAmountEditText.getText().clear();
        mAmountEditText.clearFocus();

        mReasonEditText.getText().clear();
        mReasonEditText.clearFocus();

        // the following code will hide the keyboard after plus or minus button has been pressed
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);

    }

    /**
     * Updates the walletValue inside the UI and then calls the updateBalance, which will update
     * the db with the new value. Before updating the wallet value, check whether the wallet balance
     * is sufficient for this transaction.
     *
     * @param value the amount that will be added or substracted to the wallet balance
     */
    public boolean updateWalletValue(double value){
        if (mWalletValue + value >= 0){
            mWalletValue += value;
            mWalletValueTextView.setText(String.valueOf(FormatUtils.roundTo2Decimals(mWalletValue)));
            BalanceDbHelper.updateBalanceDb(mWalletValue, "Wallet", mBalanceDb);
            return true;
        }else{
            return false;
        }
    }

    /**
     * Swaps the cursor in the RecentTransactions Fragment to update its contents.
     */
    public void swapRecentTransactionsCursor(){
        RecentTransactionsFragment fragment = (RecentTransactionsFragment) viewPagerAdapter.getItem(0);
        fragment.swapCursor();
    }


    /**
     * Swaps the cursor in the Calendar Fragment to update its contents.
     */
    public void swapCalendarCursor(){
        CalendarFragment fragment = (CalendarFragment) viewPagerAdapter.getItem(1);
        fragment.swapCursor();
    }


    /**
     * Listener which is implemented to allow the Recent Transactions Fragment have an effect on
     * MainActivity. More specifically, whenever a transaction is deleted by swiping it inside the
     * Recent Transactions Fragment, this functions is called to modify the Wallet balance and also
     * swap the Calendar cursor to keep all the fragments in sync.
     *
     * @param amount The amount that will be added to the wallet value when an item of the Recent
     *               Transactions fragment is deleted.
     */
    @Override
    public void changeWalletValueOnTransactionDeleted(double amount) {
        mWalletValue -= amount;
        mWalletValueTextView.setText(String.valueOf(round(mWalletValue * 100.0) / 100.0));

        // Swap the calendar cursor to make sure that whenever a transaction is deleted, the calendar
        // fragment gets updated too.
        swapCalendarCursor();

        BalanceDbHelper.updateBalanceDb(mWalletValue,"Wallet",mBalanceDb);
    }
}
