package com.example.kostas.phineas;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.kostas.phineas.data.BalanceDbHelper;
import com.example.kostas.phineas.data.TransactionItem;
import com.example.kostas.phineas.data.TransactionsContract;
import com.example.kostas.phineas.data.TransactionsDbHelper;


/**
 * Created by Kostas on 20/5/2017.
 */

public class RecentTransactionsFragment extends Fragment implements
        RecentTransactionsAdapter.ListItemClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener{

    private deleteTransactionWalletValueChangeListener mListener;
    private RecyclerView mTransactionsListRecyclerView;
    private RecentTransactionsAdapter mAdapter;
    private SQLiteDatabase mTransactionsDb;
    private SQLiteDatabase mBalanceDb;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try{
            mListener = (deleteTransactionWalletValueChangeListener) getActivity();
        } catch (ClassCastException e){

        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.recent_transactions_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        /** Initialize the SQLiteDatabases and retrieve all the transactions and balances */
        TransactionsDbHelper mTransactionsDbHelper = new TransactionsDbHelper(getContext());
        mTransactionsDb = mTransactionsDbHelper.getWritableDatabase();
        Cursor cursor = TransactionsDbHelper.getAllTransactions(mTransactionsDb);

        BalanceDbHelper mBalanceDbHelper = new BalanceDbHelper(getContext());
        mBalanceDb = mBalanceDbHelper.getWritableDatabase();

        /** Initialize the RecyclerView and assign a linear layout manager to it */
        mTransactionsListRecyclerView = (RecyclerView)view.findViewById(R.id.rv_transactions);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL,false);
        mTransactionsListRecyclerView.setLayoutManager(layoutManager);

        mTransactionsListRecyclerView.setHasFixedSize(true);

        /** Initialize the viewPagerAdapter, sending the cursor containing all the transactions */
        mAdapter = new RecentTransactionsAdapter(this, cursor);
        mTransactionsListRecyclerView.setAdapter(mAdapter);


        /** Create an ItemTouchHelper for the recycler view. This helper allows the user to delete
         * a transaction by swiping it left or right inside the recycler view.*/
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                long id = (long) viewHolder.itemView.getTag();
                removeTransactionFromDatabase(id);
            }
        }).attachToRecyclerView(mTransactionsListRecyclerView);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
//        displayAccountBalance(sharedPreferences.getBoolean(getString(R.string.pref_display_account_balance_key),true));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * Gets called whenever a transaction is about to be deleted. Displays an alert dialog
     * giving the user the option to reverse the effect this transaction had on the wallet balance
     * or cancel the removal of the transaction.
     *
     * @param transactionId the id of the item that is about to be deleted
     */
    private void removeTransactionFromDatabase(final long transactionId){
        final Context context = getContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.remove_transaction_alert_dialog_title));
        builder.setMessage(context.getResources().getString(R.string.remove_transaction_alert_dialog_message));

        // Cancel button. Do nothing to the transaction.
        builder.setPositiveButton(context.getResources().getString(R.string.remove_transaction_alert_dialog_cancel_button),
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        mAdapter.swapCursor(TransactionsDbHelper.getAllTransactions(mTransactionsDb));
                        mListener.changeWalletValueOnTransactionDeleted(0);
                        dialog.cancel();
                    }
                });

        // Yes button. Remove the transaction and reverse the effect it had on the wallet balance.
        builder.setNeutralButton(context.getResources().getString(R.string.remove_transaction_alert_dialog_yes_button),
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        Cursor c = TransactionsDbHelper.getTransaction(mTransactionsDb,transactionId);
                        if (!c.moveToFirst())
                            return;

                        double amount = c.getDouble(c.getColumnIndex(TransactionsContract.TransactionEntry.COLUMN_AMOUNT));
                        c.close();
                        mListener.changeWalletValueOnTransactionDeleted(amount);

                        TransactionsDbHelper.removeTransactionFromDatabase(mTransactionsDb,transactionId);

                        mAdapter.swapCursor(TransactionsDbHelper.getAllTransactions(mTransactionsDb));
                    }
                });

        // No button. Remove the transaction but do not reverse its effect on the wallet balance.
        builder.setNegativeButton(context.getResources().getString(R.string.remove_transaction_alert_dialog_no_button),
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        TransactionsDbHelper.removeTransactionFromDatabase(mTransactionsDb,transactionId);
                        mAdapter.swapCursor(TransactionsDbHelper.getAllTransactions(mTransactionsDb));
                        mListener.changeWalletValueOnTransactionDeleted(0);
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(getResources().getString(R.string.pref_date_format_key))){
            mAdapter.swapCursor(TransactionsDbHelper.getAllTransactions(mTransactionsDb));
        } else if(key.equals(getResources().getString(R.string.pref_time_format_key))) {
            mAdapter.swapCursor(TransactionsDbHelper.getAllTransactions(mTransactionsDb));
        }
    }

    /**
     * Implementation of the function defined in the RecentTransactionsAdapter. Gets called whenever
     * an item of the list is clicked, to start an Explicitly called activity with all the information
     * on the specific item.
     *
     * @param clickedItem The index of the item that was clicked.
     */
    @Override
    public void onListItemClicked(TransactionItem clickedItem) {
        Intent intent = new Intent(getContext(),TransactionDetailsActivity.class);

        intent.putExtra(TransactionsContract.TransactionEntry._ID, clickedItem.mId);
        intent.putExtra(TransactionsContract.TransactionEntry.COLUMN_NAME, clickedItem.mName);
        intent.putExtra(TransactionsContract.TransactionEntry.COLUMN_AMOUNT, String.valueOf(clickedItem.mAmount));
        intent.putExtra(TransactionsContract.TransactionEntry.COLUMN_CATEGORY, clickedItem.mCategory);
        intent.putExtra(TransactionsContract.TransactionEntry.COLUMN_DESCRIPTION, clickedItem.mDescription);
        intent.putExtra(TransactionsContract.TransactionEntry.COLUMN_DATETIME, clickedItem.mDatetime);
        intent.putExtra(TransactionsContract.TransactionEntry.COLUMN_OPERATOR, clickedItem.mOperator);
        intent.putExtra(MainActivity.MAKE_DETAIL_FIELDS_EDITABLE, false);

        startActivity(intent);
    }


    /**
     * Gets called by mainActivity to access the adapter's swapCursor function and update its content
     */
    public void swapCursor(){
        mAdapter.swapCursor(TransactionsDbHelper.getAllTransactions(mTransactionsDb));
    }

    /**
     * Interface listener implemented by MainActivity to allow the fragment change some contents of
     * views inside the main activity.
     */
    public interface deleteTransactionWalletValueChangeListener{
        void changeWalletValueOnTransactionDeleted(double amount);
    }
}


