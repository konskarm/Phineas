package com.example.kostas.phineas;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.kostas.phineas.data.TransactionsDbHelper;

/**
 * Created by Kostas on 20/5/2017.
 */

public class CalendarFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener{
    private RecyclerView mCalendarListRecyclerView;
    private CalendarAdapter mAdapter;
    private SQLiteDatabase mTransactionsDb;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.calendar_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /** Initialize the SQLiteDatabases and retrieve all the transactions and balances */
        TransactionsDbHelper mTransactionsDbHelper = new TransactionsDbHelper(getContext());
        mTransactionsDb = mTransactionsDbHelper.getWritableDatabase();
        Cursor cursor = TransactionsDbHelper.getTotalExpensesPerDay(mTransactionsDb);



        /** Initialize the RecyclerView and assign a linear layout manager to it */
        mCalendarListRecyclerView = (RecyclerView)view.findViewById(R.id.rv_calendar);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL,false);
        mCalendarListRecyclerView.setLayoutManager(layoutManager);

        mCalendarListRecyclerView.setHasFixedSize(true);

        /** Initialize the viewPagerAdapter, sending the cursor containing all the transactions */
        mAdapter = new CalendarAdapter(cursor);
        mCalendarListRecyclerView.setAdapter(mAdapter);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(getResources().getString(R.string.pref_date_format_key))){
            mAdapter.swapCursor(TransactionsDbHelper.getTotalExpensesPerDay(mTransactionsDb));
        } else if(key.equals(getResources().getString(R.string.pref_time_format_key))) {
            mAdapter.swapCursor(TransactionsDbHelper.getTotalExpensesPerDay(mTransactionsDb));
        }
    }

    /**
     * Gets called by mainActivity to access the adapter's swapCursor function and update its content
     */
    public void swapCursor(){
        mAdapter.swapCursor(TransactionsDbHelper.getTotalExpensesPerDay(mTransactionsDb));
    }
}
