package com.example.fatoumeh.shumanatorbookstore;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fatoumeh.shumanatorbookstore.data.InventoryContract.InventoryDB;


public class NewInventoryActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>  {

    private final int ADD_INVENTORY=2;
    private TextView tvProductName, tvPrice, tvQuantity, tvSupplierName, tvSupplierPhone;
    private Button increaseButton, decreaseButton, savedButton, discardButton;
    private boolean fieldsEditted=false;

    private View.OnTouchListener fieldTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            fieldsEditted = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_inventory);
        setTitle(getString(R.string.add_inventory));

        tvProductName = (EditText) findViewById(R.id.edit_product_name);
        tvPrice = (EditText) findViewById(R.id.edit_product_price);
        tvQuantity = (EditText) findViewById(R.id.edit_product_quantity);
        tvSupplierName = (EditText) findViewById(R.id.edit_supplier_name);
        tvSupplierPhone = (EditText) findViewById(R.id.edit_supplier_phone);

        //set the listener in case any of these change and back/up is pressed
        tvProductName.setOnTouchListener(fieldTouchListener);
        tvPrice.setOnTouchListener(fieldTouchListener);
        tvQuantity.setOnTouchListener(fieldTouchListener);
        tvSupplierName.setOnTouchListener(fieldTouchListener);
        tvSupplierPhone.setOnTouchListener(fieldTouchListener);

        increaseButton=findViewById(R.id.increase_button);
        increaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strQty=tvQuantity.getText().toString();
                if (!TextUtils.isEmpty(strQty)) {
                    int qty = Integer.parseInt(strQty);
                    qty++;
                    tvQuantity.setText(String.valueOf(qty));
                    //this is not caught in OnTouch so manually setting it here
                    fieldsEditted=true;
                } else {
                    //if quantity field is currently blank, we set it to 0;
                    int qty=0;
                    tvQuantity.setText(String.valueOf(qty));
                    fieldsEditted=true;
                }
            }
        });
        decreaseButton=findViewById(R.id.decrease_button);
        decreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strQty=tvQuantity.getText().toString();
                if (!TextUtils.isEmpty(strQty)) {
                    int qty = Integer.parseInt(strQty);
                    if (qty>=1) {
                        qty--;
                        tvQuantity.setText(String.valueOf(qty));
                        //this is not caught in OnTouch so manually setting it here
                        fieldsEditted=true;
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.invalid_quantity),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    //if quantity field is currently blank, we set it to 0;
                    int qty=0;
                    tvQuantity.setText(String.valueOf(qty));
                    fieldsEditted=true;
                }
            }
        });
        savedButton = findViewById(R.id.save_button);
        savedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if it's false, we stay put until user is done inserting all fields
                boolean saveState=saveInventory();
                if (saveState) {
                    finish();
                }
            }
        });
        discardButton=findViewById(R.id.discard_button);
        discardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetFields();
                finish();
            }
        });
        getLoaderManager().initLoader(ADD_INVENTORY,null,this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==android.R.id.home) {
            if (!fieldsEditted) {
                NavUtils.navigateUpFromSameTask(NewInventoryActivity.this);
                return true;
            } else {
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(NewInventoryActivity.this);
                            }
                        };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
            }
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.stay_here, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int someID) {
                // User clicked the "Stay here" button, so dismiss the dialog
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (!fieldsEditted) {
            super.onBackPressed();
            return;
        }
        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private boolean saveInventory() {
        String productName=tvProductName.getText().toString().trim();
        String supplierName=tvSupplierName.getText().toString().trim();
        String supplierPhone=tvSupplierPhone.getText().toString().trim();
        String strPrice=tvPrice.getText().toString().trim();
        String strQuantity=tvQuantity.getText().toString().trim();

        if (TextUtils.isEmpty(productName) || TextUtils.isEmpty(supplierName) ||
                TextUtils.isEmpty(supplierPhone) || TextUtils.isEmpty(strPrice) ||
                TextUtils.isEmpty(strQuantity)){
            Toast.makeText(this, getString(R.string.missing_fields),
                    Toast.LENGTH_SHORT).show();
            //return false since save is not successful
            return false;
        }

        //we know that price and quantity are available since we didnt return
        Double price=Double.valueOf(strPrice);
        Integer quantity = Integer.valueOf(strQuantity);

        ContentValues valuesToSave = new ContentValues();
        valuesToSave.put(InventoryDB.COLUMN_PRODUCT_NAME, productName);
        valuesToSave.put(InventoryDB.COLUMN_PRICE, price);
        valuesToSave.put(InventoryDB.COLUMN_QUANTITY, quantity);
        valuesToSave.put(InventoryDB.COLUMN_SUPPLIER_NAME, supplierName);
        valuesToSave.put(InventoryDB.COLUMN_SUPPLIER_PHONE, supplierPhone);

        Uri insertUri = getContentResolver().insert(InventoryDB.CONTENT_URI, valuesToSave);
        if (insertUri!=null) {
            return true;
        }
        return false;
    }

    private void resetFields() {
        tvProductName.setText("");
        tvPrice.setText("");
        tvQuantity.setText("");
        tvSupplierName.setText("");
        tvSupplierPhone.setText("");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String [] projection = {
                InventoryDB._ID,
                InventoryDB.COLUMN_PRODUCT_NAME,
                InventoryDB.COLUMN_PRICE,
                InventoryDB.COLUMN_QUANTITY,
                InventoryDB.COLUMN_SUPPLIER_NAME,
                InventoryDB.COLUMN_SUPPLIER_PHONE
        };
        return new CursorLoader(this,InventoryDB.CONTENT_URI,projection,null,
                null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        resetFields();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        resetFields();
    }
}
