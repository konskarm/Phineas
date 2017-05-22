package com.example.kostas.phineas;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.kostas.phineas.Utilities.FormatUtils;
import com.example.kostas.phineas.data.TransactionsContract.TransactionEntry;

/**
 * Created by Kostas on 21/5/2017.
 */

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.DayOfWeekViewHolder> {

    private Cursor mCursor;
    private Context mContext;

    public CalendarAdapter(Cursor cursor){
        mCursor = cursor;
    }

    @Override
    public CalendarAdapter.DayOfWeekViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();


        /** The layout describing the new view handled by the ViewHolder */
        int layoutIdForListItem = R.layout.calendar_list_item;

        /** Inflate the layout */
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem,parent,shouldAttachToParentImmediately);

        /** Create the viewHolder based on that layout that we inflated and return it */
        return new DayOfWeekViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CalendarAdapter.DayOfWeekViewHolder holder, int position) {
        holder.bind(position);
//        long id = mCursor.getLong(mCursor.getColumnIndex(TransactionEntry._ID));
//        holder.itemView.setTag(id);
    }

    @Override
    public int getItemCount() {
        if (mCursor == null){return 0;}
        return mCursor.getCount();
    }

    public class DayOfWeekViewHolder extends RecyclerView.ViewHolder{

        TextView listItemDayTextView;

        public DayOfWeekViewHolder(View itemView) {
            super(itemView);
            listItemDayTextView = (TextView) itemView.findViewById(R.id.tv_calendar_day);
            mContext = itemView.getContext();
        }


        /*
        The cursor in this adapter has only 2 columns:
            DATE column, which has dates in it.
            total column, which has the total amount spend for each day.
         */
        public void bind(int listIndex){
            if (!mCursor.moveToPosition(listIndex))
                return;

            String dateString = mCursor.getString(mCursor.getColumnIndex(TransactionEntry.COLUMN_DATE));

            //Get preferred date and time formats based on the user preferences
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            String dateKey = mContext.getResources().getString(R.string.pref_date_format_key);
            String dateDefault = mContext.getResources().getString(R.string.pref_date_format_default_value);
            String dateFormat = sharedPreferences.getString(dateKey,dateDefault);

            // Format date and time from the default SQL format to the format selected by the user
            dateString = FormatUtils.switchDateTimeFormat(dateString,
                    FormatUtils.DEFAULT_SQL_DATE_FORMAT,
                    dateFormat);

            double total = mCursor.getDouble(mCursor.getColumnIndex(TransactionEntry.COLUMN_SUM_TOTAL));
            listItemDayTextView.setText(dateString + ": " + String.valueOf(FormatUtils.roundTo2Decimals(total)));
        }
    }

    public void swapCursor(Cursor newCursor){
        if (mCursor != null){
            mCursor.close();
        }
        mCursor = newCursor;

        if(newCursor != null){
            this.notifyDataSetChanged();
        }
    }

}
