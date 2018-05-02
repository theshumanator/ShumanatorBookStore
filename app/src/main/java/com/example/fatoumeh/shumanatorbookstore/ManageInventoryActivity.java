package com.example.fatoumeh.shumanatorbookstore;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.Toast;

import com.example.fatoumeh.shumanatorbookstore.data.InventoryContract.InventoryDB;

public class ManageInventoryActivity extends AppCompatActivity  implements LoaderManager.LoaderCallbacks<Cursor> {

    private Uri productUri;
    private final int EDIT_INVENTORY=3;
    private EditText productName, price, quantity, supplierName, supplierPhone;
    private boolean hasInventoryChanged=false;
    private Button increaseQty, decreaseQty, saveChanges, discardChanges, deleteInventory, orderInventory;

    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            hasInventoryChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_inventory);
        setTitle(getString(R.string.manage_inventory));
        productUri=getIntent().getData();

        productName=findViewById(R.id.edit_text_product_name);
        price=findViewById(R.id.edit_text_product_price);
        quantity=findViewById(R.id.edit_text_product_quantity);
        supplierName=findViewById(R.id.edit_text_supplier_name);
        supplierPhone=findViewById(R.id.edit_text_supplier_phone);

        productName.setOnTouchListener(touchListener);
        price.setOnTouchListener(touchListener);
        quantity.setOnTouchListener(touchListener);
        supplierName.setOnTouchListener(touchListener);
        supplierPhone.setOnTouchListener(touchListener);

        increaseQty=findViewById(R.id.increaseQty);
        increaseQty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strQty=quantity.getText().toString();
                if (!TextUtils.isEmpty(strQty)) {
                    int qty = Integer.parseInt(strQty);
                    qty++;
                    quantity.setText(String.valueOf(qty));
                    //this is not caught in OnTouch so manually setting it here
                    hasInventoryChanged=true;
                } else {
                    /*unlikely but just in case quantity field is currently blank
                     (corrupt data in db), we set it to 0;*/
                    int qty=0;
                    quantity.setText(String.valueOf(qty));
                    hasInventoryChanged=true;
                }
            }
        });
        decreaseQty=findViewById(R.id.decreaseQty);
        decreaseQty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strQty=quantity.getText().toString();
                if (!TextUtils.isEmpty(strQty)) {
                    int qty = Integer.parseInt(strQty);
                    if (qty>=1) {
                        qty--;
                        quantity.setText(String.valueOf(qty));
                        //this is not caught in OnTouch so manually setting it here
                        hasInventoryChanged=true;
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.invalid_quantity),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    //if quantity field is currently blank, we set it to 0;
                    int qty=0;
                    quantity.setText(String.valueOf(qty));
                    hasInventoryChanged=true;
                }
            }
        });
        saveChanges=findViewById(R.id.saveChanges);
        saveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean saveState=saveInventory();
                if (saveState) {
                    finish();
                }
            }
        });
        discardChanges=findViewById(R.id.discardChanges);
        discardChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetFields();
                finish();
            }
        });
        deleteInventory=findViewById(R.id.deleteInventory);
        deleteInventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteConfirmationDialog();
            }
        });
        orderInventory=findViewById(R.id.orderInventory);
        orderInventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNumber=supplierPhone.getText().toString();
                Intent orderIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
                startActivity(orderIntent);
            }
        });
        getLoaderManager().initLoader(EDIT_INVENTORY,null, this);
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
        return new CursorLoader(this,productUri,projection,null,
                null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            productName.setText(cursor.getString(cursor.getColumnIndexOrThrow(InventoryDB.COLUMN_PRODUCT_NAME)));
            price.setText(cursor.getString(cursor.getColumnIndexOrThrow(InventoryDB.COLUMN_PRICE)));
            quantity.setText(cursor.getString(cursor.getColumnIndexOrThrow(InventoryDB.COLUMN_QUANTITY)));
            supplierName.setText(cursor.getString(cursor.getColumnIndexOrThrow(InventoryDB.COLUMN_SUPPLIER_NAME)));
            supplierPhone.setText(cursor.getString(cursor.getColumnIndexOrThrow(InventoryDB.COLUMN_SUPPLIER_PHONE)));
        } else {
            resetFields();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        resetFields();
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the inventory.
                deleteInventory();
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the inventory.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteInventory () {
        int rowsDeleted = getContentResolver().delete(productUri,
                null, null);
        if (rowsDeleted <= 0) {
            Toast.makeText(this, getString(R.string.editor_delete_inventory_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.editor_delete_inventory_successful),
                    Toast.LENGTH_SHORT).show();
        }
        finish();
    }
    private boolean saveInventory() {
        String name=productName.getText().toString().trim();
        String sName=supplierName.getText().toString().trim();
        String sPhone=supplierPhone.getText().toString().trim();
        String strPrice=price.getText().toString().trim();
        String strQuantity=quantity.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(sName) ||
                TextUtils.isEmpty(sPhone) || TextUtils.isEmpty(strPrice) ||
                TextUtils.isEmpty(strQuantity)){
            Toast.makeText(this, getString(R.string.missing_fields),
                    Toast.LENGTH_SHORT).show();
            //return false since save is not successful
            return false;
        }

        //we know that price and quantity are available since we didnt return
        Double dblPrice=Double.valueOf(strPrice);
        Integer intQuantity = Integer.valueOf(strQuantity);

        ContentValues valuesToSave = new ContentValues();
        valuesToSave.put(InventoryDB.COLUMN_PRODUCT_NAME, name);
        valuesToSave.put(InventoryDB.COLUMN_PRICE, dblPrice);
        valuesToSave.put(InventoryDB.COLUMN_QUANTITY, intQuantity);
        valuesToSave.put(InventoryDB.COLUMN_SUPPLIER_NAME, sName);
        valuesToSave.put(InventoryDB.COLUMN_SUPPLIER_PHONE, sPhone);

        int rowsAffected = getContentResolver().update(productUri, valuesToSave,
                null,null);
        if (rowsAffected>0) {
            return true;
        } else {
            return false;
        }
    }

    private void resetFields() {
        productName.setText("");
        price.setText("");
        quantity.setText("");
        supplierName.setText("");
        supplierPhone.setText("");
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.stay_here, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int someID) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (!hasInventoryChanged) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };

        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==android.R.id.home) {
            if (!hasInventoryChanged) {
                NavUtils.navigateUpFromSameTask(ManageInventoryActivity.this);
                return true;
            } else {
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(ManageInventoryActivity.this);
                            }
                        };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
            }
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}