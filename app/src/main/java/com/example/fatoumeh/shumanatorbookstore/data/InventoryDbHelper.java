package com.example.fatoumeh.shumanatorbookstore.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.fatoumeh.shumanatorbookstore.data.InventoryContract.InventoryDB;

/**
 * Created by fatoumeh on 03/03/2018.
 */

public class InventoryDbHelper extends SQLiteOpenHelper {

    public InventoryDbHelper(Context context){
        super(context, InventoryDB.DB_NAME,null, InventoryDB.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String SQL_CREATE_INVENTORY_TABLE="CREATE TABLE " + InventoryDB.TABLE_NAME +
                " ("+ InventoryDB._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                InventoryDB.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, " +
                InventoryDB.COLUMN_PRICE + " REAL NOT NULL, " +
                InventoryDB.COLUMN_QUANTITY + " INTEGER NOT NULL, " +
                InventoryDB.COLUMN_SUPPLIER_NAME + " TEXT NOT NULL, " +
                InventoryDB.COLUMN_SUPPLIER_PHONE + " TEXT NOT NULL);";
        db.execSQL(SQL_CREATE_INVENTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String SQL_DROP_TABLE="DROP TABLE IF EXISTS " + InventoryDB.TABLE_NAME;
        db.execSQL(SQL_DROP_TABLE);
    }
}
