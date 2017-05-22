package com.example.kostas.phineas.data;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Created by Kostas on 4/5/2017.
 */

public class TransactionItem {
    public String mName;
    public double mAmount;
    public String mCategory;
    public String mDescription;
    public String mDatetime;
    public String mOperator;
    public long mId;

    public TransactionItem(long id, String name, double amount, String category, String description,
                           String datetime, String operator){
        mName = name;
        mAmount = amount;
        mCategory = category;
        mDescription = description;
        mDatetime = datetime;
        mOperator = operator;
        mId = id;
    }


}
