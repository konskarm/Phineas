package com.example.kostas.phineas;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.kostas.phineas.Utilities.FormatUtils;
import com.example.kostas.phineas.data.TransactionItem;
import com.example.kostas.phineas.data.TransactionsContract.TransactionEntry;


/**
 * Created by Kostas on 4/5/2017.
 */

public class RecentTransactionsAdapter extends RecyclerView.Adapter<RecentTransactionsAdapter.TransactionViewHolder> {

    final private ListItemClickListener mOnClickListener;
    private Cursor mCursor;
    private Context mContext;

    /**
     * Constructor. Sets an item click listener on each item of the Recent Transactions List.
     *
     * @param itemClickListener the ListItemClickListener interface, which is implemented in MainActivity
     */

    public RecentTransactionsAdapter(ListItemClickListener itemClickListener, Cursor cursor){
        mOnClickListener = itemClickListener;
        mCursor = cursor;
    }

    /**
     * Inner class to hold the views needed to display a single item in the recycler view
     */
    class TransactionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView listItemDatetimeTextView;
        TextView listItemAmountTextView;
        TextView listItemNameTextView;
//        TextView listItemNameLabelTextView;
        FrameLayout listItemAmountFrameLayout;
        /**
         * Constructor for our View Holder. Within this constructor, we get a reference to our
         * views.
         *
         * @param itemView The view we inflated in onCreateViewHolder
         */
        public TransactionViewHolder(View itemView) {
            super(itemView);
            listItemDatetimeTextView = (TextView) itemView.findViewById(R.id.tv_item_transaction_datetime);
            listItemAmountTextView = (TextView) itemView.findViewById(R.id.tv_item_transaction_amount);
            listItemNameTextView = (TextView) itemView.findViewById(R.id.tv_item_transaction_name);
//            listItemNameLabelTextView = (TextView) itemView.findViewById(R.id.list_item_transaction_name_label);
            listItemAmountFrameLayout = (FrameLayout) itemView.findViewById(R.id.fl_item_transaction_amount);
            mContext = itemView.getContext();
            itemView.setOnClickListener(this);
        }

        /**
         * Function that gets called in RecentTransactionsAdapter.onBindViewHolder. In this method we
         * update the contents of the ViewHolder to display the transaction details for this
         * particular position, using the listIndex argument.
         *
         * @param listIndex The index of an item in the Transactions List
         */
        private void bind(int listIndex){
            if (!mCursor.moveToPosition(listIndex))
                return;

            /**
            * Get the datetime string and format it based on the selected date and time format
            * from the shared preferences
            * */
            // Get the datetime string from the cursor
            String datetimeString = mCursor.getString(mCursor.getColumnIndex(TransactionEntry.COLUMN_DATETIME));

            //Get preferred date and time formats based on the user preferences
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            String dateKey = mContext.getResources().getString(R.string.pref_date_format_key);
            String dateDefault = mContext.getResources().getString(R.string.pref_date_format_default_value);
            String dateFormat = sharedPreferences.getString(dateKey,dateDefault);

            String timeKey = mContext.getResources().getString(R.string.pref_time_format_key);
            String timeDefault = mContext.getResources().getString(R.string.pref_time_format_default_value);
            String timeFormat = sharedPreferences.getString(timeKey, timeDefault);

            // Format date and time from the default SQL format to the format selected by the user
            datetimeString = FormatUtils.switchDateTimeFormat(datetimeString,
                    FormatUtils.DEFAULT_SQL_DATETIME_FORMAT,
                    dateFormat + " " + timeFormat);
            listItemDatetimeTextView.setText(datetimeString);

            /**
             * Get the operator and the amount. If the transaction is positive, also display the
             * operator (negative values already display the - operator).
             */
            String operator = mCursor.getString(mCursor.getColumnIndex(TransactionEntry.COLUMN_OPERATOR));
            double amount = mCursor.getDouble(mCursor.getColumnIndex(TransactionEntry.COLUMN_AMOUNT));
            String toDisplay = "";
            if (operator.equals("+"))
                toDisplay += operator;
            toDisplay += String.valueOf(amount);
            listItemAmountTextView.setText(toDisplay);


            /**
             * Get the reason field and display it
             */
            String name = mCursor.getString(mCursor.getColumnIndex(TransactionEntry.COLUMN_NAME));
            if (name != null && !name.equals("")){
                listItemNameTextView.setText(name);
//                listItemNameLabelTextView.setVisibility(View.VISIBLE);
            }
            else{
                /*
                 * Whenever we have a case where we check if something is null and based on that
                 * we change the value of a view inside the viewHolder, we then need to also reset
                 * that field, or else recyclerView may keep a value from a previous recycled
                 * representation. In this case, if we don't set the listItemNameTextView to "",
                 * the next time it is recycled it may keep a value from the recycled view and display
                 * false information.
                 */

                listItemNameTextView.setText("");
//                listItemNameLabelTextView.setVisibility(View.INVISIBLE);
            }

            /**
             * Switch the color of the amount text view based on the operator
             */
            switch (operator){
                case "+":
//                    listItemAmountTextView.setTextColor(ContextCompat.getColor(mContext,R.color.colorPlusButton));
                    listItemAmountFrameLayout.setBackgroundColor(ContextCompat.getColor(mContext,R.color.colorPlusButton));
                    break;
                case "-":
//                    listItemAmountTextView.setTextColor(ContextCompat.getColor(mContext,R.color.colorMinusButton));
                    listItemAmountFrameLayout.setBackgroundColor(ContextCompat.getColor(mContext,R.color.colorMinusButton));
                    break;
            }


        }

        /**
         * Function called every time a transaction item of the recyclerView is clicked.
         * After we get the position of the view that was clicked we call the interfaces
         * onClickListener's onListItemClicked(int position) which is implemented by
         * MainActivity.
         *
         * @param view The specific item that was clicked
         */
        @Override
        public void onClick(View view) {

            int clickedPosition = getAdapterPosition();
            mCursor.moveToPosition(clickedPosition);

            int id = mCursor.getInt(mCursor.getColumnIndex(TransactionEntry._ID));
            String name = mCursor.getString(mCursor.getColumnIndex(TransactionEntry.COLUMN_NAME));
            double amount = mCursor.getDouble(mCursor.getColumnIndex(TransactionEntry.COLUMN_AMOUNT));
            String category = mCursor.getString(mCursor.getColumnIndex(TransactionEntry.COLUMN_CATEGORY));
            String operator = mCursor.getString(mCursor.getColumnIndex(TransactionEntry.COLUMN_OPERATOR));
            String description = mCursor.getString(mCursor.getColumnIndex(TransactionEntry.COLUMN_DESCRIPTION));
            String datetimeString = mCursor.getString(mCursor.getColumnIndex(TransactionEntry.COLUMN_DATETIME));

            TransactionItem clickedItem = new TransactionItem(id, name, amount, category,
                    description, datetimeString, operator);
            mOnClickListener.onListItemClicked(clickedItem);
        }
    }

    @Override
    public RecentTransactionsAdapter.TransactionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();


        /** The layout describing the new view handled by the ViewHolder */
        int layoutIdForListItem = R.layout.transaction_list_item;

        /** Inflate the layout */
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem,parent,shouldAttachToParentImmediately);

        /** Create the viewHolder based on that layout that we inflated and return it */
        return new TransactionViewHolder(view);
    }


    /**
     * onBindViewHolder gets called by the recyclerView to display the data at the specified
     * position. Simply calls the ViewHolders bind method, passing in the position.
     *
     * @param holder The view holder which we should update to represent the contents of the item
     *               at the given position in the data set.
     * @param position The position of the item within the viewPagerAdapter's data set.
     */
    @Override
    public void onBindViewHolder(RecentTransactionsAdapter.TransactionViewHolder holder, int position) {
        holder.bind(position);
        long id = mCursor.getLong(mCursor.getColumnIndex(TransactionEntry._ID));
        holder.itemView.setTag(id);
    }

    /**
     * This function returns the number of items to display.
     *
     * @return The number of items available in our transaction list.
     */
    @Override
    public int getItemCount() {
        if (mCursor == null){return 0;}
        return mCursor.getCount();
    }



    public interface ListItemClickListener {
        void onListItemClicked(TransactionItem transactionItem);
    }

    /**
     * Swap the cursor to update the contents of the RecyclerView and then call
     * notifyDataSetChanged
     *
     * @param newCursor the new Cursor
     */
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
