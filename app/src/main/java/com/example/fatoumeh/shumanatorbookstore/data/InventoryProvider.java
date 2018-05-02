package com.example.fatoumeh.shumanatorbookstore.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;

import com.example.fatoumeh.shumanatorbookstore.R;
import com.example.fatoumeh.shumanatorbookstore.data.InventoryContract.InventoryDB;

/**
 * Created by fatoumeh on 06/03/2018.
 */

public class InventoryProvider extends ContentProvider {

    public InventoryDbHelper dbHelper;
    private static final int INVENTORY=100;
    private static final int INVENTORY_ID=101;

    private final int NO_ROWS_AFFECTED=0;
    private final int MIN_CONTENT_SIZE=1;
    private final double MIN_PRICE=0;
    private final int MIN_QTY=0;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY, INVENTORY);
        uriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY+"/#", INVENTORY_ID);
    }

    @Override
    public boolean onCreate() {
        dbHelper=new InventoryDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        Cursor cursor;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int uriMatchCode = uriMatcher.match(uri);
        switch (uriMatchCode){
            case (INVENTORY):
                cursor=db.query(InventoryDB.TABLE_NAME,projection,selection,selectionArgs,null,
                        null,sortOrder);
                break;
            case (INVENTORY_ID):
                selection=InventoryDB._ID+"=?";
                selectionArgs=new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor=db.query(InventoryDB.TABLE_NAME,projection,selection,selectionArgs,null,
                        null,sortOrder);
                break;
            default:
                throw new IllegalArgumentException(getContext().getString(R.string.query_unknown_uri)  + uri);
        }
        //set up the notification  so any changes here notify up the content resolver
        cursor.setNotificationUri(getContext().getContentResolver(),uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return InventoryDB.CONTENT_LIST_TYPE;
            case INVENTORY_ID:
                return InventoryDB.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException(getContext().getString(R.string.unknown_uri) + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        int matchCode = uriMatcher.match(uri);
        switch (matchCode){
            case INVENTORY:
                return insertInventory(uri,contentValues);
            default:
                throw new IllegalArgumentException(getContext().getString(R.string.insert_unknown_uri) + uri);
        }
    }

    private Uri insertInventory(Uri uri, ContentValues contentValues) {
        boolean isInputValid=validateInput(contentValues);
        if (isInputValid) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            long newRowID = db.insert(InventoryDB.TABLE_NAME,null,contentValues);
            if (newRowID!=-1) {
                Toast.makeText(getContext(), getContext().getString(R.string.insert_successful), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), getContext().getString(R.string.insert_not_successful), Toast.LENGTH_SHORT).show();
                return null;
            }

            //notify all listeners that content has changed
            getContext().getContentResolver().notifyChange(uri,null);
            //return uri with id of the new row appended to it
            return ContentUris.withAppendedId(uri,newRowID);
        } else {
            return null;
        }

    }

    private boolean validateInput(ContentValues contentValues) {
        //if only one row then ensure it's the quantity only (for sale button action)
        if (contentValues.size()==MIN_CONTENT_SIZE) {
            if (contentValues.containsKey(InventoryDB.COLUMN_QUANTITY)) {
                Integer quantity=contentValues.getAsInteger(InventoryDB.COLUMN_QUANTITY);
                if (quantity==null || quantity<MIN_QTY) {
                    Toast.makeText(getContext(), getContext().getString(R.string.invalid_quantity),
                            Toast.LENGTH_SHORT).show();
                    return false;
                }
            } else {
                Toast.makeText(getContext(), getContext().getString(R.string.missing_fields), Toast.LENGTH_SHORT).show();
                return false;
            }
        } else {
            String productName=contentValues.getAsString(InventoryDB.COLUMN_PRODUCT_NAME);
            if (TextUtils.isEmpty(productName)){
                Toast.makeText(getContext(), getContext().getString(R.string.invalid_pname), Toast.LENGTH_SHORT).show();
                return false;
            }

            Double price=contentValues.getAsDouble(InventoryDB.COLUMN_PRICE);
            if (price==null || price<=MIN_PRICE) {
                Toast.makeText(getContext(), getContext().getString(R.string.invalid_price), Toast.LENGTH_SHORT).show();
                return false;
            }

            Integer quantity=contentValues.getAsInteger(InventoryDB.COLUMN_QUANTITY);
            if (quantity==null || quantity<MIN_QTY) {
                Toast.makeText(getContext(), getContext().getString(R.string.invalid_quantity), Toast.LENGTH_SHORT).show();
                return false;
            }
            String supplierName=contentValues.getAsString(InventoryDB.COLUMN_SUPPLIER_NAME);
            if (TextUtils.isEmpty(supplierName)){
                Toast.makeText(getContext(), getContext().getString(R.string.invalid_sname), Toast.LENGTH_SHORT).show();
                return false;
            }

            String supplierPhone=contentValues.getAsString(InventoryDB.COLUMN_SUPPLIER_PHONE);
            if (TextUtils.isEmpty(supplierPhone)){
                Toast.makeText(getContext(), getContext().getString(R.string.invalid_sphone), Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int rowsDeleted=0;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int matchCode=uriMatcher.match(uri);
        switch (matchCode){
            case INVENTORY:
                rowsDeleted=db.delete(InventoryDB.TABLE_NAME,selection, selectionArgs);
                break;
            case INVENTORY_ID:
                selection=InventoryDB._ID+"=?";
                selectionArgs=new String [] {String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted=db.delete(InventoryDB.TABLE_NAME,selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException(getContext().getString(R.string.delete_unknown_uri)  + uri);
        }
        //notify all listeners that content has changed
        getContext().getContentResolver().notifyChange(uri,null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        int matchCode=uriMatcher.match(uri);
        switch (matchCode) {
            case INVENTORY:
                return updateInventory(uri, contentValues, selection, selectionArgs);
            case INVENTORY_ID:
                selection = InventoryDB._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateInventory(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException(getContext().getString(R.string.update_unknown_uri) + uri);
        }
    }

    private int updateInventory(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        if (contentValues.size() == NO_ROWS_AFFECTED) {
            return NO_ROWS_AFFECTED;
        }
        boolean isInputValid=validateInput(contentValues);
        if (isInputValid) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            int rowsUpdated=db.update(InventoryDB.TABLE_NAME,contentValues,selection,selectionArgs);
            if (rowsUpdated>NO_ROWS_AFFECTED) {
                //notify all listeners that content has changed
                getContext().getContentResolver().notifyChange(uri,null);
            }
            return rowsUpdated;
        } else {
          return NO_ROWS_AFFECTED;
        }
    }
}