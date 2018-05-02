package com.example.fatoumeh.shumanatorbookstore;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fatoumeh.shumanatorbookstore.data.InventoryContract.InventoryDB;

public class InventoryActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private final int INVENTORY_LOADER=1;
    private InventoryCursorAdapter inventoryCursorAdapter;
    private Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        ListView lvItems=findViewById(R.id.inventory_list);
        View emptyView=findViewById(R.id.empty_view);
        lvItems.setEmptyView(emptyView);
        inventoryCursorAdapter=new InventoryCursorAdapter(this,cursor);
        lvItems.setAdapter(inventoryCursorAdapter);

        FloatingActionButton fabNew = findViewById(R.id.fab);
        fabNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newIntent = new Intent(InventoryActivity.this, NewInventoryActivity.class);
                startActivity(newIntent);
            }
        });

        getLoaderManager().initLoader(INVENTORY_LOADER,null, this);
    }

    public void onManageClicked(View button)
    {
        //We need to iterate a few steps up to get to the listview
        View parentRow = (View) button.getParent().getParent().getParent();
        //i set the tag on the button to be the _ID from the db so i can use it below
        int id = Integer.parseInt(String.valueOf(button.getTag()));
        ListView listView = (ListView) parentRow.getParent();
        Intent manageIntent = new Intent(InventoryActivity.this, ManageInventoryActivity.class);
        Uri manageUri= ContentUris.withAppendedId(InventoryDB.CONTENT_URI, id);
        manageIntent.setData(manageUri);
        startActivity(manageIntent);
    }

    public void decreaseQuantity(View button) {
        View parentRow = (View) button.getParent().getParent().getParent();
        TextView tvQuantity = parentRow.findViewById(R.id.product_quantity);
        int quantity = Integer.parseInt(tvQuantity.getText().toString().trim());
        //check if we can actually decrease the quantity
        if (quantity>0) {
            quantity--;
            tvQuantity.setText(String.valueOf(quantity));
            updateQuantity(button, quantity);
            return;
        } else {
            Toast.makeText(this, getString(R.string.no_more_sales),
                    Toast.LENGTH_SHORT).show();
            return;
        }
    }

    private void updateQuantity(View button, int newQuantity) {
        View parentRow = (View) button.getParent().getParent().getParent();
        int id = Integer.parseInt(String.valueOf(button.getTag()));
        Uri updateUri= ContentUris.withAppendedId(InventoryDB.CONTENT_URI, id);

        ContentValues values=new ContentValues();
        values.put(InventoryDB.COLUMN_QUANTITY, newQuantity);
        int rowsUpdated = getContentResolver().update(updateUri, values,
                null, null);
        if (rowsUpdated>0) {
            Toast.makeText(this, getString(R.string.sales_successful), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_inventory, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_insert_dummy_data:
                insertInventory();
                return true;
            case R.id.action_delete_all_entries:
                deleteAllInventory();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String [] projection = {
                InventoryDB._ID,
                InventoryDB.COLUMN_PRODUCT_NAME,
                InventoryDB.COLUMN_PRICE,
                InventoryDB.COLUMN_QUANTITY
        };
        String sortOrder=InventoryDB.COLUMN_QUANTITY + " " + getString(R.string.sort_asc);
        return new CursorLoader(this,InventoryDB.CONTENT_URI,projection,null,
                null,sortOrder);
    }

    //using changeCursor b/c it will flush old cursor
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor newCursor) {
        inventoryCursorAdapter.changeCursor(newCursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        inventoryCursorAdapter.changeCursor(null);
    }

    //insert dummy book for now
    private void insertInventory() {
        String dummyProductName=getString(R.string.dummy_pname);
        Double dummyPrice=Double.valueOf(getString(R.string.dummy_price));
        Integer dummyQuantity=Integer.valueOf(getString(R.string.dummy_quantity));
        String dummySupplierName=getString(R.string.dummy_sname);
        String dummyPhone=getString(R.string.dummy_sphone);

        ContentValues valuesToInsert = new ContentValues();
        valuesToInsert.put(InventoryDB.COLUMN_PRODUCT_NAME, dummyProductName);
        valuesToInsert.put(InventoryDB.COLUMN_PRICE, dummyPrice);
        valuesToInsert.put(InventoryDB.COLUMN_QUANTITY, dummyQuantity);
        valuesToInsert.put(InventoryDB.COLUMN_SUPPLIER_NAME, dummySupplierName);
        valuesToInsert.put(InventoryDB.COLUMN_SUPPLIER_PHONE, dummyPhone);
        Uri insertUri = getContentResolver().insert(InventoryDB.CONTENT_URI, valuesToInsert);
    }

    //delete all inventory
    private void deleteAllInventory() {
        int rowsDeleted=getContentResolver().delete(InventoryDB.CONTENT_URI,null,null);
        if (rowsDeleted>0) {
            Toast.makeText(this,getString(R.string.inventory_bulk_deleted), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this,getString(R.string.inventory_bulk_not_deleted), Toast.LENGTH_SHORT).show();
        }
    }
}
