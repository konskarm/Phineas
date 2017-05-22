package com.example.kostas.phineas.data;

import android.provider.BaseColumns;

/**
 * Created by Kostas on 8/5/2017.
 */

public final class BalanceContract  {
    public static final class BalanceEntry implements BaseColumns{
        public static final String TABLE_NAME = "balances";

        public static final String COLUMN_NAME = "name";

        public static final String COLUMN_AMOUNT = "amount";
    }
}
