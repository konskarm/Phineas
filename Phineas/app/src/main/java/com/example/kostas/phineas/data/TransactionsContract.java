package com.example.kostas.phineas.data;

import android.provider.BaseColumns;

/**
 * Created by Kostas on 5/5/2017.
 */

public final class TransactionsContract {
    public static final class TransactionEntry implements BaseColumns{

        public static final String TABLE_NAME = "transactions";

        public static final String COLUMN_NAME = "name";

        public static final String COLUMN_AMOUNT = "amount";

        public static final String COLUMN_CATEGORY = "category";

        public static final String COLUMN_DESCRIPTION = "description";

        public static final String COLUMN_DATETIME = "datetime";

        public static final String COLUMN_OPERATOR = "operator";

        public static final String COLUMN_SUM_TOTAL = "total";

        public static final String COLUMN_DATE = "simpleDate";
    }
}
