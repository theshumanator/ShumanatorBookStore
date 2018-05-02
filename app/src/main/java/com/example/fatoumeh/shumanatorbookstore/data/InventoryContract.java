package com.example.fatoumeh.shumanatorbookstore.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by fatoumeh on 03/03/2018.
 */

public final class InventoryContract {

    private InventoryContract(){}

    //content authority
    public static final String CONTENT_AUTHORITY = "com.example.fatoumeh.shumanatorbookstore";

    //base uri with content authority
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    //path to each table
    public static final String PATH_INVENTORY = "inventory";

    public static final class InventoryDB implements BaseColumns {
        //path for a list of inventories
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        //path for a single inventory
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        //full usi for: content://com.example.fatoumeh.shumanatorbookstore/inventory
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);

        //constants on db level
        public final static String DB_NAME="inventory.db";
        public final static int DB_VERSION=1;
        public final static String TABLE_NAME = "inventory";
        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_PRODUCT_NAME = "product_name";
        public final static String COLUMN_QUANTITY = "quantity";
        public final static String COLUMN_PRICE = "price";
        public final static String COLUMN_SUPPLIER_NAME = "supplier_name";
        public final static String COLUMN_SUPPLIER_PHONE = "supplier_phone";
    }
}
